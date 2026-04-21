package com.jozedev.bankapp.client.service;

import com.jozedev.bankapp.client.dto.Cliente;
import com.jozedev.bankapp.client.dto.ClienteCreate;
import com.jozedev.bankapp.client.dto.ClientePartialUpdate;
import com.jozedev.bankapp.client.dto.ClienteUpdate;
import com.jozedev.bankapp.client.exception.ClientNotFoundException;
import com.jozedev.bankapp.client.mapper.ClientMapper;
import com.jozedev.bankapp.client.model.Client;
import com.jozedev.bankapp.client.repository.ClientRepository;
import com.jozedev.bankapp.client.util.Messages;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clienteMapper;

    @Override
    public Flux<Cliente> findAll() {
        return clientRepository.findAll().map(clienteMapper::toDto);
    }

    @Override
    public Mono<Cliente> findClientById(Long clientId) {
        return clientRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new ClientNotFoundException(Messages.CLIENT_NOT_FOUND, clientId)))
                .map(clienteMapper::toDto);
    }

    @Override
    public Mono<Cliente> saveClient(Mono<ClienteCreate> client) {
        return client
            .flatMap(c -> clientRepository.save(clienteMapper.toEntity(c)))
            .map(clienteMapper::toDto);
    }

    @Override
    public Mono<Cliente> updateClient(Long clientId, Mono<ClienteUpdate> clienteUpdate) {
        return clientRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new ClientNotFoundException(Messages.CLIENT_NOT_FOUND, clientId)))
                .flatMap(c -> clienteUpdate)
                .map(c -> {
                    Client client = clienteMapper.toEntity(c);
                    client.setClienteId(clientId);
                    return client;
                })
                .flatMap(clientRepository::save)
                .map(clienteMapper::toDto);
    }

    @Override
    public Mono<Cliente> partialUpdate(Long clientId, Mono<ClientePartialUpdate> clientePartialUpdate) {
        return clientRepository.findById(clientId)
            .switchIfEmpty(Mono.error(new ClientNotFoundException(Messages.CLIENT_NOT_FOUND, clientId)))
            .zipWith(clientePartialUpdate)
            .map(tuple -> {
                var client = tuple.getT1();
                var partial = tuple.getT2();

                client.setEstado(partial.getEstado());
                return client;
            })
            .flatMap(clientRepository::save)
            .map(clienteMapper::toDto);
    }

    @Override
    public Mono<Void> deleteClient(Long clientId) {
        return clientRepository.existsById(clientId)
            .filter(Boolean::booleanValue)
            .switchIfEmpty(Mono.error(new ClientNotFoundException(Messages.CLIENT_NOT_FOUND, clientId)))
            .flatMap(exists -> clientRepository.deleteById(clientId));
    }
}
