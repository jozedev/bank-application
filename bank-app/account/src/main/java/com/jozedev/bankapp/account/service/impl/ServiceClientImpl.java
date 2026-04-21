package com.jozedev.bankapp.account.service.impl;

import com.jozedev.bankapp.account.configuration.ServicesProperties;
import com.jozedev.bankapp.account.service.ServiceClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class ServiceClientImpl implements ServiceClient {

    private final WebClient webClient;
    private final ServicesProperties servicesProperties;

    public ServiceClientImpl(@Qualifier("clientWebClient") WebClient webClient, ServicesProperties servicesProperties) {
        this.webClient = webClient;
        this.servicesProperties = servicesProperties;
    }

    @Override
    public <T> Mono<T> getForEntity(String serviceKey, Class<T> responseType, Object... uriVariables) {
        String serviceUri = servicesProperties.getClient().getServices().get(serviceKey);

        return webClient.get()
                .uri(serviceUri, uriVariables)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(WebClientResponseException.class, ex -> ex.getStatusCode().equals(HttpStatus.NOT_FOUND) ?
                        Mono.empty() : Mono.error(ex));
    }
}
