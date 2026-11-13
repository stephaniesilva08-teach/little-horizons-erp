package com.littlehorizons.erp.util;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Formats optional name components without nulls or repeated whitespace. */
public final class NameFormatter {

    private NameFormatter() {
    }

    public static String fullName(String... parts) {
        return Arrays.stream(parts)
                .filter(part -> part != null && !part.isBlank())
                .map(String::strip)
                .collect(Collectors.joining(" "));
    }
}
