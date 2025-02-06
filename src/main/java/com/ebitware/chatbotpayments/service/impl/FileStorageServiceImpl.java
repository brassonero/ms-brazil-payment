package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.config.properties.FileStorageProperties;
import com.ebitware.chatbotpayments.constants.ApiErrors;
import com.ebitware.chatbotpayments.exception.ApiException;
import com.ebitware.chatbotpayments.model.UploadFileResponse;
import com.ebitware.chatbotpayments.service.FileStorageService;
import com.ebitware.chatbotpayments.util.CustomStringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Objects;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    @Value("${app.compress.to.type.image}")
    String typeImage;

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new ApiException(ApiErrors.FILE_CANT_CREATE_DIRECTORY("es"));
        }
    }

    /**
     * This method allows saving a file in the server file system
     *
     * @param file is from MultipartFile type and it represents the file we want to
     *             save
     * @return Returns the name of saved file
     */
    @Override
    public String storeFile(MultipartFile file, String prefixName, String language) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String newName = prefixName + CustomStringUtils.normalizeName(fileName);

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newName;
        } catch (IOException e) {
            throw new ApiException(ApiErrors.FILE_CANT_STORED(language));
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName, String language) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ApiException(ApiErrors.FILE_NOT_FOUND(language));
            }
        } catch (MalformedURLException ex) {
            throw new ApiException(ApiErrors.FILE_NOT_FOUND(language));
        }
    }

    @Override
    public String getPathUploasFiles() {
        return this.fileStorageLocation.toString();
    }

    /**
     * This method allows saving a file in the server file system
     *
     * @param file is from MultipartFile type and it represents the file we want to
     *             save
     * @return Returns the name of saved file
     */
    @Override
    public String storeFileInputStream(MultipartFile file, String prefixName, String language) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String newName = prefixName + CustomStringUtils.normalizeName(fileName);

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newName);
            File testFile = new File(targetLocation.toAbsolutePath().toString());
            InputStream fileStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(fileStream, testFile);
            return newName;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApiException(ApiErrors.FILE_CANT_STORED(language));
        }
    }

    /**
     * This method validates whether the file is an image
     *
     * @param file is from array Byte type and it represents the file we want to
     *             save
     * @return Returns the name of saved file
     */
    @Override
    public boolean isCompressFile(MultipartFile file) {
        try {
            Image imageAux = ImageIO.read(file.getInputStream());
            return !ObjectUtils.isEmpty(imageAux);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * this method compresses images to reduce their size and convert it to the
     * format that is specified in the configuration file, the property is
     * app.compress.to.type.image
     */
    @Override
    public UploadFileResponse compressFile(float imageQuality, MultipartFile file, String context,
                                           StringBuilder fileDownloadUriBuilder, String originalFileName, String prefixName, String language) {
        String extension = FilenameUtils.getExtension(originalFileName);
        boolean isCompressed = false, isTypeChanged = false;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            InputStream inputStream = file.getInputStream();
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName(typeImage);

            if (!imageWriters.hasNext()) {
                throw new ApiException(ApiErrors.FILE_INVALID_PATH(language));
            }

            ImageWriter imageWriter = imageWriters.next();
            bufferedImage = this.removeTransparency(bufferedImage);

            if (!extension.equals(typeImage.toUpperCase()) || !extension.equals(typeImage.toLowerCase())) {
                isTypeChanged = true;
                originalFileName = originalFileName.replace(extension, typeImage);
            }

            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            imageWriter.setOutput(imageOutputStream);

            ImageWriteParam param = imageWriter.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                isCompressed = true;
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(imageQuality);
            }

            imageWriter.write(null, new IIOImage(bufferedImage, null, null), param);
            byte[] imageBytes = outputStream.toByteArray();

            inputStream.close();
            outputStream.close();
            imageOutputStream.close();
            imageWriter.dispose();

            String fileName = this.storeFileByteArray(imageBytes, originalFileName, prefixName, language);
            fileDownloadUriBuilder.append(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApiException(ApiErrors.FILE_INVALID_PATH(language));
        }
        String fileDownloadUri = fileDownloadUriBuilder.toString();
        return new UploadFileResponse(originalFileName, fileDownloadUri, "image/"+typeImage, file.getSize());
    }

    /**
     * This method allows saving a file in the server file system
     */
    @Override
    public String storeFileByteArray(byte[] imageBytes, String fileName, String prefixName, String language) {
        InputStream targetStream = new ByteArrayInputStream(imageBytes);
        String newName = prefixName + CustomStringUtils.normalizeName(fileName);

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newName);
            File testFile = new File(targetLocation.toAbsolutePath().toString());
            FileUtils.copyInputStreamToFile(targetStream, testFile);
            return newName;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApiException(ApiErrors.FILE_CANT_STORED(language));
        }
    }

    /**
     * This method removes transparency from images and places a white background
     */
    @Override
    public BufferedImage removeTransparency(BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel().hasAlpha()) {
            // create a blank, RGB, same width and height
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            // draw a white background and puts the originalImage on it.
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
            return newBufferedImage;
        }
        return bufferedImage;
    }
}
