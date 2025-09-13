package org.solcation.solcation_be.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class GeminiConfig {

    private final GeminiProperties geminiProperties;

    @Bean
    public WebClient geminiWebClient(WebClient.Builder builder) {
        if (geminiProperties.getApiKey() == null || geminiProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("GOOGLE_API_KEY 환경변수가 비어있어");
        }
        return builder
                .baseUrl(geminiProperties.getEndpoint())
                .defaultHeaders(h -> h.setContentType(MediaType.APPLICATION_JSON))
                .filter((request, next) -> {
                    URI withKey = UriComponentsBuilder.fromUri(request.url())
                            .queryParam("key", geminiProperties.getApiKey())
                            .build(true).toUri();
                    ClientRequest newReq = ClientRequest.from(request).url(withKey).build();
                    return next.exchange(newReq);
                })
                .build();
    }
}
