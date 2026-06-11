package com.english.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckDictationRequest {

    @NotNull(message = "sentenceId is required")
    private Long sentenceId;

    @NotBlank(message = "userInput is required")
    private String userInput;
}
