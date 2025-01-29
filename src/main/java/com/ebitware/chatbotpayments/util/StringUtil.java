package com.ebitware.chatbotpayments.util;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StringUtil {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String ALL_CHARS = LOWERCASE + UPPERCASE + NUMBERS;

    public String randomString(int length, String type) {
        String charset = switch (type) {
            case "letters" -> LOWERCASE + UPPERCASE;
            case "numbers" -> NUMBERS;
            default -> ALL_CHARS;
        };

        return new Random()
                .ints(length, 0, charset.length())
                .mapToObj(charset::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public String removeAccents(String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        }
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^a-zA-Z0-9]", "");
    }
}
