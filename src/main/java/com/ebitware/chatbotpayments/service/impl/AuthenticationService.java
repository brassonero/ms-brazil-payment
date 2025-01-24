package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.model.LoginCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
public class AuthenticationService {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_32 = "3b141c29b7b22bf20bbefdaaa378dc560af2e90f8bc773c2d89265bbbe7fdb73";
    private static final String IV_16 = "06e6ba0f54d04131c5dfed99de9f0e7f";

    public LoginCredentials decryptCredentials(String encryptedLogin) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    Hex.decodeHex(KEY_32.toCharArray()),
                    "AES"
            );
            IvParameterSpec ivSpec = new IvParameterSpec(
                    Hex.decodeHex(IV_16.toCharArray())
            );

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedLogin));
            String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);

            return new ObjectMapper().readValue(decrypted, LoginCredentials.class);

        } catch (Exception e) {
            log.error("Failed to decrypt login credentials", e);
            throw new RuntimeException(e);
        }
    }
}
