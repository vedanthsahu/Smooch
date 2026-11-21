package com.incidentcommand.backend.simulator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SimulatorClientConfig {

    /** Bean name "simulatorRestClient" -- injected by matching parameter name,
     * since there's also an "aiEngineRestClient" RestClient bean. */
    @Bean
    RestClient simulatorRestClient(@Value("${incidentcommand.simulator.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
