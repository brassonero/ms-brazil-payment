package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.constants.ApiErrors;
import com.ebitware.chatbotpayments.constants.LogsErrors;
import com.ebitware.chatbotpayments.exception.ApiException;
import com.ebitware.chatbotpayments.model.ApiResponse;
import com.ebitware.chatbotpayments.model.UploadFileResponse;
import com.ebitware.chatbotpayments.repository.billing.FormSubmissionRepository;
import com.ebitware.chatbotpayments.service.FileStorageService;
import com.ebitware.chatbotpayments.service.LogHelper;
import com.ebitware.chatbotpayments.util.CustomStringUtils;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/media")
public class FileApiController {

    private final String className = getClass().getSimpleName();
    private final FileStorageService fileStorage;
    private final LogHelper logHelper;
    public static final int FIVE_MB_SIZE = 9242880;
    private final FormSubmissionRepository formSubmissionRepository;

    @Value("${app.domain}")
    String startHttps;

    @Value("${server.servlet.context-path}")
    String context;

    @Value("${app.compress.quality}")
    float compressImageQuality;

    @Autowired
    public FileApiController(FileStorageService fileStorage, LogHelper logHelper, FormSubmissionRepository formSubmissionRepository) {
        this.fileStorage = fileStorage;
        this.logHelper = logHelper;
        this.formSubmissionRepository = formSubmissionRepository;
    }

    @PostMapping
    public ApiResponse<UploadFileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "personId", required = false) Integer personId,
            @RequestHeader(name = "Accept-Language", required = false) String language) {

        String methodName = "uploadFile";
        String urlEndpoint = "/media/";
        StringBuilder fileDownloadUriBuilder = new StringBuilder(startHttps);

        logHelper.startLog(null, methodName, LogsErrors.POST, "POST", -1, urlEndpoint, className);

        if (file.getSize() > FIVE_MB_SIZE) {
            logHelper.finishLog("File weight exceeds 5 megabytes", LogsErrors.SEND_POST_ERROR);
            throw new ApiException(ApiErrors.FILE_INVALID_SIZE(language));
        }

        LocalDateTime date = LocalDateTime.now();
        long millisOfDay = date.toLocalTime().toNanoOfDay() / 1_000_000;
        String prefixName = String.valueOf(millisOfDay);

        UploadFileResponse fileUploaded;
        String aux = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String originalFilename = CustomStringUtils.normalizeName(aux);
        fileDownloadUriBuilder.append(context).append("/media/");

        if (this.fileStorage.isCompressFile(file)) {
            fileUploaded = this.fileStorage.compressFile(compressImageQuality, file, context,
                    fileDownloadUriBuilder, originalFilename, prefixName, language);
        } else {
            String fileName = this.fileStorage.storeFileInputStream(file, prefixName, language);
            fileDownloadUriBuilder.append(fileName);
            String fileDownloadUri = fileDownloadUriBuilder.toString();
            fileUploaded = new UploadFileResponse(originalFilename, fileDownloadUri,
                    file.getContentType(), file.getSize());
        }

        // Update logo_url if personId is provided
        if (personId != null && fileStorage.isCompressFile(file)) {
            int updated = formSubmissionRepository.updateLogoUrl(personId, fileUploaded.getFileDownloadUri());
            log.debug("Updated logo_url for person ID {}: {}", personId, updated > 0);
        }

        logHelper.finishLog(fileUploaded, LogsErrors.SEND_POST_OK);
        return new ApiResponse<>(HttpStatus.OK, fileUploaded);
    }

    @PostMapping("/multiple")
    public List<ApiResponse<UploadFileResponse>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestHeader(name = "Accept-Language", required = false) String language) {
        return Arrays.stream(files)
                .map(file -> uploadFile(file, null, language))
                .collect(Collectors.toList());
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName,
            @RequestHeader(name = "Accept-Language", required = false) String language,
            HttpServletRequest request) {

        String methodName = "downloadFile";
        String urlEndpoint = "/media/" + fileName;

        logHelper.startLog(null, methodName, LogsErrors.GET, "GET", -1, urlEndpoint, className);

        try {
            Resource resource = fileStorage.loadFileAsResource(fileName, language);
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            logHelper.finishLog(resource.getFilename(), LogsErrors.SEND_GET_OK);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            logHelper.finishLog(ex.getMessage(), LogsErrors.SEND_GET_ERROR);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
