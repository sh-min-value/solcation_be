package org.solcation.solcation_be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models}")
    private String endpoint;

    @Bean
    public WebClient geminiWebClient(WebClient.Builder builder) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GOOGLE_API_KEY 환경변수가 비어있어");
        }
        return builder
                .baseUrl(endpoint)
                .defaultHeaders(h -> h.setContentType(MediaType.APPLICATION_JSON))
                .filter((request, next) -> {
                    URI withKey = UriComponentsBuilder.fromUri(request.url())
                            .queryParam("key", apiKey)
                            .build(true).toUri();
                    ClientRequest newReq = ClientRequest.from(request).url(withKey).build();
                    return next.exchange(newReq);
                })
                .build();
    }

    public String geminiModel() {
        return model;
    }
}