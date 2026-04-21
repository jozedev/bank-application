package com.jozedev.bankapp.account.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(ServicesProperties.class)
@PropertySource("classpath:endpoints.properties")
public class AccountConfiguration {

    @Bean
    @Qualifier("clientWebClient")
    public WebClient webClient(ServicesProperties servicesProperties, WebClient.Builder builder) {
        return builder.baseUrl(servicesProperties.getClient().getBaseUri()).build();
    }
}
