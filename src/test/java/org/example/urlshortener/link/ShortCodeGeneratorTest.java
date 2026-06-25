package org.example.urlshortener.link;

import org.example.urlshortener.config.ShortLinkProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator =
            new ShortCodeGenerator(new ShortLinkProperties("http://localhost:8080", 6, 8, 30));

    @Test
    void generatesCodeWithinConfiguredLengthBounds() {
        for (int i = 0; i < 500; i++) {
            String code = generator.generate();
            assertThat(code.length()).isBetween(6, 8);
            assertThat(code).matches("[A-Za-z0-9]+");
        }
    }

    @Test
    void generatesDifferentCodes() {
        String first = generator.generate();
        String second = generator.generate();
        assertThat(first).isNotEqualTo(second);
    }
}
