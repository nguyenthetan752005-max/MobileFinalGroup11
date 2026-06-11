package com.english.learning.service.integration.scoring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.english.learning.entity.SpeakingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiScoringService implements PronunciationScoringGateway {

    // 1. URL chÃ­nh thá»©c cá»§a Groq API
    @Value("${groq.api.url}")
    private String GROQ_API_URL;

    // ÄIá»€N API KEY Cá»¦A Báº N VÃ€O ÄÃ‚Y (Trong thá»±c táº¿ nÃªn Ä‘á»ƒ á»Ÿ file
    // application.properties)
    @Value("${groq.api.key}")
    private String GROQ_API_KEY;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpeakingResult scoreSpeaking(String expectedText, String userText) {
        try {
            // 2. Setup Header (Sá»­ dá»¥ng Bearer Token)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(GROQ_API_KEY);

            // Giá»›i háº¡n sá»‘ chá»¯ trong lá»i khuyÃªn Ä‘á»ƒ AI cháº¡y nhanh hÆ¡n ná»¯a
            // Giá»›i háº¡n sá»‘ chá»¯ vÃ  Bá»” SUNG THANG ÄIá»‚M (Rubric) RÃ• RÃ€NG
            String systemPrompt = "You are an English pronunciation grading API. Compare the 'expected text' and 'user text'. "
                    + "SCORING RUBRIC: Calculate the score strictly based on the percentage of matched words. "
                    + "(Number of correctly spoken words / Total expected words) * 100. "
                    + "Example: If 2 out of 3 words are correct, the score must be around 66. "
                    + "Ignore minor STT formatting errors ('10' vs 'ten', 'where\\'s' vs 'where is'). "
                    + "Return ONLY a valid JSON object with keys: 'score' (int 0-100), 'explanation' (string pointing out the exact mispronounced word, max 10 words), and 'advice' (string, max 10 words). STRICTLY JSON ONLY.";
            String userPrompt = "Expected text: '" + expectedText + "'. User text: '" + userText + "'";

            // 3. XÃ¢y dá»±ng Body gá»­i Ä‘i
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama-3.1-8b-instant"); // Model Llama 3 8B cá»±c ká»³ thÃ´ng minh vÃ  siÃªu tá»‘c
            requestBody.put("stream", false);

            // Ã‰p AI tráº£ lá»i mÃ¡y mÃ³c, chuáº©n xÃ¡c, khÃ´ng tá»± Ã½ sÃ¡ng táº¡o bay bá»•ng
            requestBody.put("temperature", 0.0);

            // TÃNH NÄ‚NG Äáº¶C BIá»†T: Ã‰p API cá»§a Groq báº¯t buá»™c pháº£i nháº£ ra Ä‘á»‹nh dáº¡ng JSON
            requestBody.put("response_format", Map.of("type", "json_object"));

            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 4. Gá»­i Request
            ResponseEntity<String> response = restTemplate.postForEntity(GROQ_API_URL, entity, String.class);

            // 5. Äá»c káº¿t quáº£ (Cáº¥u trÃºc cá»§a Groq/OpenAI náº±m trong máº£ng choices)
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String aiResponseContent = rootNode.path("choices").get(0).path("message").path("content").asText();

            // 6. Parse tháº³ng ra Object (KhÃ´ng cáº§n hÃ m cleanJsonResponse ná»¯a vÃ¬ Ä‘Ã£ dÃ¹ng
            // response_format)
            JsonNode scoreNode = objectMapper.readTree(aiResponseContent);
            int score = scoreNode.path("score").asInt(0);
            String explanation = scoreNode.path("explanation").asText("No explanation");
            String advice = scoreNode.path("advice").asText("Keep practicing!");

            SpeakingResult result = new SpeakingResult();
            result.setScore(score);
            result.setFeedback(explanation + " " + advice);
            return result;

        } catch (Exception e) {
            System.err.println("Lá»—i gá»i Groq AI: " + e.getMessage());
            SpeakingResult result = new SpeakingResult();
            result.setScore(0);
            result.setFeedback("System error Please try again later.");
            return result;
        }
    }
}

