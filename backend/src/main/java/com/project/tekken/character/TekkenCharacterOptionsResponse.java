package com.project.tekken.character;

import java.util.List;

public record TekkenCharacterOptionsResponse(
        List<TekkenCharacterOption> characters
) {
}
