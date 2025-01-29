package com.ebitware.chatbotpayments.service.impl;

import com.fasterxml.jackson.databind.JsonMappingException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.FormSubmissionRequest;
import com.ebitware.chatbotpayments.repository.billing.FormSubmissionRepository;
import com.ebitware.chatbotpayments.service.ConfirmationTokenService;
import com.ebitware.chatbotpayments.service.EmailService;
import com.ebitware.chatbotpayments.service.FormSubmissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FormSubmissionServiceImpl implements FormSubmissionService {

    private final FormSubmissionRepository formSubmissionRepository;
    private final MinioClient minioClient;
    private final EmailService emailService;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    @Transactional
    public void saveSubmission(FormSubmissionRequest form, String logoUrl) {

        formSubmissionRepository.saveSubmissionForm(form, logoUrl);

        String token = confirmationTokenService.generateToken();
        confirmationTokenService.saveToken(form.getCorporateEmail(), token);

        String confirmationUrl = "https://management-dev.broadcasterbot.com/paymentsApi/email/confirm?token=" + token;
        String emailBody = String.format(
                "Hello %s,\n\n" +
                        "Thank you for your submission. Please click the link below to confirm your email:\n" +
                        "%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "Best regards,\n" +
                        "Your Application Team",
                form.getDisplayName(),
                confirmationUrl
        );

        emailService.sendEmail(
                form.getCorporateEmail(),
                "Confirm your email address",
                emailBody
        );
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            String fileName = generateUniqueFileName(Objects.requireNonNull(file.getOriginalFilename()));
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("logos")
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = originalFilename.substring(
                originalFilename.lastIndexOf("."));
        return timestamp + extension;
    }

    @Override
    public List<String> listObjects() {
        List<String> objects = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket("logos").build()
            );

            for (Result<Item> result : results) {
                objects.add(result.get().objectName());
            }
        } catch (MinioException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return objects;
    }

    @Override
    public InputStream downloadFile(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getObject(
                GetObjectArgs.builder().bucket("logos").object(fileName).build()
        );
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !formSubmissionRepository.emailExists(email);
    }
}
