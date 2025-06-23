package com.microproject.linktoolkit.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.springframework.stereotype.Component;

@Component
public class AliasGenerator {
    private static final int DEFAULT_ALIAS_LENGTH = 7;

    /**
     * Generates a short, URL-friendly, random alias.
     * @return A random string of 7 characters.
     */
    public String generate() {
        // Generates a random ID with URL-friendly characters (A-Z, a-z, 0-9, _, -)
        return NanoIdUtils.randomNanoId(
                NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                NanoIdUtils.DEFAULT_ALPHABET,
                DEFAULT_ALIAS_LENGTH
        );
    }
}