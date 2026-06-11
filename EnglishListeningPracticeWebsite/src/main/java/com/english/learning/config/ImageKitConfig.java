package com.english.learning.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class ImageKitConfig {

    private static final String MANAGEMENT_API_BASE_URL = "https://api.imagekit.io";
    private static final String UPLOAD_API_BASE_URL = "https://upload.imagekit.io";

    @Bean("imageKitManagementClient")
    public RestClient imageKitManagementClient(@Value("${imagekit.private-key:}") String privateKey) {
        return buildClient(MANAGEMENT_API_BASE_URL, privateKey);
    }

    @Bean("imageKitUploadClient")
    public RestClient imageKitUploadClient(@Value("${imagekit.private-key:}") String privateKey) {
        return buildClient(UPLOAD_API_BASE_URL, privateKey);
    }

    private RestClient buildClient(String baseUrl, String privateKey) {
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        if (StringUtils.hasText(privateKey)) {
            builder.defaultHeaders(headers -> headers.setBasicAuth(privateKey, ""));
        }
        return builder.build();
    }
}
