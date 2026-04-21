package com.jozedev.bankapp.client.service;

import com.jozedev.bankapp.client.dto.Cliente;
import com.jozedev.bankapp.client.dto.ClienteCreate;
import com.jozedev.bankapp.client.dto.ClientePartialUpdate;
import com.jozedev.bankapp.client.dto.ClienteUpdate;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClientService {

    Flux<Cliente> findAll();

    Mono<Cliente> findClientById(Long clienteId);

    Mono<Cliente> saveClient(Mono<ClienteCreate> cliente);

    Mono<Cliente> updateClient(Long clienteId, Mono<ClienteUpdate> clienteUpdate);

    Mono<Cliente> partialUpdate(Long clienteId, Mono<ClientePartialUpdate> partialCliente);

    Mono<Void> deleteClient(Long clientId);
}
