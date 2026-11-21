package com.incidentcommand.backend.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiEngineClientConfig {

    /** Investigation calls involve a model call and can take several seconds
     * (observed ~7s with a local 1B model) -- the default request factory's
     * read timeout is too short for that, so it's set explicitly here. */
    @Bean
    RestClient aiEngineRestClient(@Value("${incidentcommand.ai-engine.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
