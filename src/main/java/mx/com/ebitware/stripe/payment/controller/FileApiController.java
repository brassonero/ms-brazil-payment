package mx.com.ebitware.stripe.payment.controller;

import mx.com.ebitware.stripe.payment.constants.ApiErrors;
import mx.com.ebitware.stripe.payment.constants.LogsErrors;
import mx.com.ebitware.stripe.payment.exception.ApiException;
import mx.com.ebitware.stripe.payment.model.ApiResponse;
import mx.com.ebitware.stripe.payment.model.UploadFileResponse;
import mx.com.ebitware.stripe.payment.service.FileStorageService;
import mx.com.ebitware.stripe.payment.service.LogHelper;
import mx.com.ebitware.stripe.payment.util.CustomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/media")
public class FileApiController {

    private final String className = getClass().getSimpleName();
    private final FileStorageService fileStorage;
    private final LogHelper logHelper;
    public static final int FIVE_MB_SIZE = 9242880;

    @Value("${app.domain}")
    String startHttps;

    @Value("${server.servlet.context-path}")
    String context;

    @Value("${app.compress.quality}")
    float compressImageQuality;

    @Autowired
    public FileApiController(FileStorageService fileStorage, LogHelper logHelper) {
        this.fileStorage = fileStorage;
        this.logHelper = logHelper;
    }

    @PostMapping
    public ApiResponse<UploadFileResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                                      @RequestHeader(name = "Accept-Language", required = false) String language) {
        String methodName = "uploadFile";
        String urlEndpoint = "/media/";
        StringBuilder fileDownloadUriBuilder = new StringBuilder(startHttps);
        logHelper.startLog(null, methodName, LogsErrors.POST, "POST", -1,
                urlEndpoint, className);
        LocalDateTime date = LocalDateTime.now();
        long millisOfDay = date.toLocalTime().toNanoOfDay() / 1_000_000; // Convertir nanosegundos a milisegundos
        String prefixName = String.valueOf(millisOfDay);

        if (file.getSize() > FIVE_MB_SIZE) {
            logHelper.finishLog("File weight exceeds 5 megabytes", LogsErrors.SEND_POST_ERROR);
            throw new ApiException(ApiErrors.FILE_INVALID_SIZE(language));
        }

        UploadFileResponse fileUploaded = new UploadFileResponse();
        String aux = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String originalFilename = CustomStringUtils.normalizeName(aux);
        fileDownloadUriBuilder.append(context).append("/media/");

        if (this.fileStorage.isCompressFile(file)) {
            fileUploaded = this.fileStorage.compressFile(compressImageQuality, file, context, fileDownloadUriBuilder, originalFilename,
                    prefixName, language);
        } else {
            String fileName = this.fileStorage.storeFileInputStream(file, prefixName, language);
            fileDownloadUriBuilder.append(fileName);
            String fileDownloadUri = fileDownloadUriBuilder.toString();
            fileUploaded = new UploadFileResponse(originalFilename, fileDownloadUri, file.getContentType(),
                    file.getSize());
        }

        logHelper.finishLog(fileUploaded, LogsErrors.SEND_POST_OK);
        return new ApiResponse<>(HttpStatus.OK, fileUploaded);

    }

    @PostMapping("/multiple")
    public List<ApiResponse<UploadFileResponse>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,
                                                                     @RequestHeader(name = "Accept-Language", required = false) String language) {
        return Arrays.stream(files).map(file -> uploadFile(file, language)).collect(Collectors.toList());
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName,
                                                 @RequestHeader(name = "Accept-Language", required = false) String language, HttpServletRequest request) {
        String methodName = "downloadFile";
        String urlEndpoint = "/media/" + fileName;

        logHelper.startLog(null, methodName, LogsErrors.GET, "GET", -1, urlEndpoint, className);
        Resource resource;
        String contentType;
        try {
            resource = fileStorage.loadFileAsResource(fileName, language);
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (Exception ex) {
            logHelper.finishLog(ex.getMessage(), LogsErrors.SEND_GET_ERROR);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (Objects.requireNonNull(resource.getFilename()).contains(".docx"))
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (Objects.requireNonNull(resource.getFilename()).contains(".xlsx"))
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        logHelper.finishLog(resource.getFilename(), LogsErrors.SEND_GET_OK);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
