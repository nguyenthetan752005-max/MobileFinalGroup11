package com.english.learning.service.integration.transcription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MappingIterator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

@Service
public class WitAIAudioService implements SpeechTranscriptionGateway {

    // Äá»c URL tá»« application.properties
    @Value("${wit.api.url}")
    private String WIT_AI_URL;

    // Äá»c Token tá»« application.properties
    @Value("${wit.api.token}")
    private String WIT_AI_TOKEN;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public String transcribeAudio(MultipartFile audioFile) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        // XÃ¡c thá»±c qua Bearer Token
        headers.set("Authorization", "Bearer " + WIT_AI_TOKEN);

        // Khai bÃ¡o tháº³ng vá»›i Wit.ai Ä‘Ã¢y lÃ  file WAV chuáº©n
        headers.set("Content-Type", "audio/wav");

        // Chuyá»ƒn file audio thÃ nh byte máº£ng Ä‘á»ƒ Ä‘áº©y trá»±c tiáº¿p qua HTTP
        byte[] audioBytes = audioFile.getBytes();
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(audioBytes, headers);

        // Gá»­i yÃªu cáº§u POST tá»›i Wit.ai
        ResponseEntity<String> response = restTemplate.exchange(
                WIT_AI_URL,
                HttpMethod.POST,
                requestEntity,
                String.class);

        String responseBody = response.getBody();
        String transcribedText = "";

        if (responseBody != null && !responseBody.trim().isEmpty()) {
            try {
                // DÃ¹ng MappingIterator cá»§a Jackson Ä‘á»ƒ tá»± Ä‘á»™ng bÃ³c tÃ¡ch cÃ¡c Object JSON dÃ­nh
                // liá»n nhau
                MappingIterator<JsonNode> iterator = mapper.readerFor(JsonNode.class)
                        .readValues(responseBody);

                System.out.println("=== DEBUG: Raw Response from Wit.ai ===");
                System.out.println(responseBody);
                System.out.println("======================================");

                while (iterator.hasNextValue()) {
                    JsonNode jsonNode = iterator.nextValue();

                    // CHá»ˆ Cáº¬P NHáº¬T TEXT Náº¾U NÃ“ KHÃ”NG Bá»Š Rá»–NG
                    if (jsonNode.has("text")) {
                        String tempText = jsonNode.get("text").asText();
                        if (tempText != null && !tempText.trim().isEmpty()) {
                            transcribedText = tempText;
                        }
                    }

                    // Náº¿u gáº·p cá» is_final = true thÃ¬ dá»«ng (Ä‘Ã£ cÃ³ káº¿t quáº£ cuá»‘i cÃ¹ng)
                    if (jsonNode.has("is_final") && jsonNode.get("is_final").asBoolean()) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Lá»—i khi parse JSON tá»« Wit.ai: " + e.getMessage());
                System.err.println("Ná»™i dung raw tá»« Wit.ai:\n" + responseBody);
                throw e; // NÃ©m lá»—i ra Ä‘á»ƒ Controller báº¯t vÃ  bÃ¡o vá» UI
            }
        }

        return transcribedText.trim();
    }
}

