package com.english.learning.exception;

public class SentenceNotFoundException extends RuntimeException {

    public SentenceNotFoundException(Long sentenceId) {
        super("Sentence not found: " + sentenceId);
    }
}
