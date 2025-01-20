package mx.com.ebitware.stripe.payment.service;

import mx.com.ebitware.stripe.payment.model.UploadFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;

public interface FileStorageService {

    String storeFile(MultipartFile file, String prefixName, String language);

    Resource loadFileAsResource(String fileName, String language);

    String getPathUploasFiles();

    String storeFileInputStream(MultipartFile file, String prefixName, String language);

    boolean isCompressFile(MultipartFile file);

    UploadFileResponse compressFile(float imageQuality, MultipartFile file, String context,
                                    StringBuilder fileDownloadUriBuilder, String originalFileName, String prefixName, String language);

    BufferedImage removeTransparency(BufferedImage bufferedImage);

    String storeFileByteArray(byte[] imageBytes, String originalName, String prefixName, String language);

}
