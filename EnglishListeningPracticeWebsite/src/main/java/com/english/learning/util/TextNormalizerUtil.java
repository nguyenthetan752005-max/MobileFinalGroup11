package com.english.learning.util;

/**
 * Utility class for common text normalization and string manipulation tasks.
 * Kept separate from the `service` package because it contains domain-agnostic,
 * stateless static methods that can safely be called from any layer (Controller/Service).
 */
public class TextNormalizerUtil {

    private TextNormalizerUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Removes all punctuation from a word and converts it to lowercase.
     * Used for comparing dictation answers.
     * "Jane?" -> "jane", "Hello!" -> "hello"
     */
    public static String removePunctuationAndLowercase(String word) {
        if (word == null) return "";
        return word.replaceAll("[.,?!;:'\"-]", "").toLowerCase().trim();
    }

    /**
     * Cleans HTML tags, bracketed text, and leading hyphens from a sentence.
     * Expected output is clean text for displaying or further processing.
     */
    public static String cleanHtmlAndBrackets(String text) {
        if (text == null) return "";
        // Remove HTML tags
        String cleaned = text.replaceAll("<[^>]*>", "");
        // Remove text inside brackets (e.g., [Music], [Applause])
        cleaned = cleaned.replaceAll("\\[.*?\\]", "").trim();
        // Remove leading dialogue hyphen
        if (cleaned.startsWith("- ")) {
            cleaned = cleaned.substring(2).trim();
        }
        // Normalize multiple spaces into single space
        return cleaned.replaceAll("\\s+", " ");
    }

    /**
     * Keeps only letters and hyphens for a given word. Removes all other characters.
     * Useful for extracting proper nouns without attached punctuation.
     * "Smith," -> "Smith"
     */
    public static String keepLettersAndHyphens(String word) {
        if (word == null) return "";
        return word.replaceAll("[^a-zA-Z'-]", "");
    }
}
