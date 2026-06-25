package com.project.tekken.character;

import java.util.List;
import java.util.Map;

public record TekkenCharacterOption(
        String id,
        String name,
        String displayName,
        Map<String, String> localizedNames,
        String assetKey,
        String imageUrl,
        List<String> aliases
) {
}
