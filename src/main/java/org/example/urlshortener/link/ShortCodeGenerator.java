package org.example.urlshortener.link;

import org.example.urlshortener.config.ShortLinkProperties;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    private static final char[] ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private final SecureRandom random = new SecureRandom();
    private final int minLength;
    private final int maxLength;

    public ShortCodeGenerator(ShortLinkProperties properties) {
        this.minLength = properties.codeMinLength();
        this.maxLength = properties.codeMaxLength();
    }

    public String generate() {
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return builder.toString();
    }
}
