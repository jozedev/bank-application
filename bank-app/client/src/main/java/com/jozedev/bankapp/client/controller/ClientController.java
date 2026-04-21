package com.jozedev.bankapp.client.controller;

import com.jozedev.bankapp.client.api.ClientesApi;
import com.jozedev.bankapp.client.dto.Cliente;
import com.jozedev.bankapp.client.dto.ClienteCreate;
import com.jozedev.bankapp.client.dto.ClientePartialUpdate;
import com.jozedev.bankapp.client.dto.ClienteUpdate;
import com.jozedev.bankapp.client.service.ClientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientesApi {

    private final ClientService clientService;

    @Override
    public Mono<ResponseEntity<Flux<Cliente>>> getClientes(final ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(clientService.findAll()));
    }

    @Override
    public Mono<ResponseEntity<Cliente>> getClienteById(
        Long clienteId,
        final ServerWebExchange exchange
    ) {
        return clientService.findClientById(clienteId).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Cliente>> createCliente(
            @Valid @RequestBody Mono<ClienteCreate> clienteCreate, 
            final ServerWebExchange exchange) {
        return clientService.saveClient(clienteCreate).map(ResponseEntity.status(HttpStatus.CREATED)::body);
    }

    @Override
    public Mono<ResponseEntity<Cliente>> updateCliente(
        Long clienteId, 
        @Valid @RequestBody Mono<ClienteUpdate> clienteUpdate, 
        final ServerWebExchange exchange
    ) {
        return clientService.updateClient(clienteId, clienteUpdate).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Cliente>> patchCliente(
        Long clienteId,
        @Valid @RequestBody Mono<ClientePartialUpdate> clientePartialUpdate,
        final ServerWebExchange exchange
    ) {
        return clientService.partialUpdate(clienteId, clientePartialUpdate).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCliente(
        Long clienteId,
        final ServerWebExchange exchange
    ) {
        return clientService.deleteClient(clienteId).map(s -> ResponseEntity.noContent().build());
    }
}
