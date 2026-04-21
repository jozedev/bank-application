package com.jozedev.bankapp.account.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "service")
@Data
public class ServicesProperties {

    private ServiceInformation client;

    @Data
    public static class ServiceInformation {
        private String baseUri;
        private Map<String, String> services;
    }
}
