package com.project.tekken.character;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TekkenCharacterCatalogServiceTest {

    @Test
    void returnsSortedCharacterOptionsForFilters() {
        TekkenCharacterCatalogService service = new TekkenCharacterCatalogService();

        TekkenCharacterOptionsResponse response = service.getCharacterOptions();

        assertThat(response.characters()).hasSizeGreaterThan(30);
        assertThat(response.characters())
                .extracting(TekkenCharacterOption::name)
                .contains("Dragunov", "Bryan", "Kazuya", "Jin", "Reina");
        assertThat(response.characters())
                .extracting(TekkenCharacterOption::displayName)
                .isSorted();
    }

    @Test
    void keepsCharacterMetadataShapeReadyForImagesAndAliases() {
        TekkenCharacterCatalogService service = new TekkenCharacterCatalogService();

        TekkenCharacterOption dragunov = service.getCharacterOptions().characters().stream()
                .filter(character -> "dragunov".equals(character.id()))
                .findFirst()
                .orElseThrow();

        assertThat(dragunov.name()).isEqualTo("Dragunov");
        assertThat(dragunov.displayName()).isEqualTo("Dragunov");
        assertThat(dragunov.localizedNames())
                .containsEntry("ko", "드라구노프")
                .containsEntry("en", "Dragunov")
                .containsEntry("ja", "ドラグノフ");
        assertThat(dragunov.assetKey()).isEqualTo("dragunov");
        assertThat(dragunov.imageUrl()).isNull();
        assertThat(dragunov.aliases()).contains("Sergei Dragunov");
    }
}
