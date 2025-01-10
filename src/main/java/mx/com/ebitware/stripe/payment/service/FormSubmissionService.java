package mx.com.ebitware.stripe.payment.service;

import mx.com.ebitware.stripe.payment.model.FormSubmissionRequest;

import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface FormSubmissionService {
    void saveSubmission(FormSubmissionRequest form, String logoUrl);
    String uploadFile(MultipartFile file);
    List<String> listObjects();
    InputStream downloadFile(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
}
