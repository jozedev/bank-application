package com.jozedev.bankapp.client.mapper;

import org.mapstruct.Mapper;

import com.jozedev.bankapp.client.dto.Cliente;
import com.jozedev.bankapp.client.dto.ClienteCreate;
import com.jozedev.bankapp.client.dto.ClienteUpdate;
import com.jozedev.bankapp.client.model.Client;

@Mapper(config = GlobalMapperConfig.class)
public interface ClientMapper {
    Cliente toDto(Client client);
    ClienteCreate toDtoCreate(Client client);
    ClienteUpdate toDtoUpdate(Client client);
    Client toEntity(Cliente cliente);
    Client toEntity(ClienteCreate clienteCreate);
    Client toEntity(ClienteUpdate clienteUpdate);

    default Cliente.GeneroEnum map(String value) {
        return value == null ? null : Cliente.GeneroEnum.fromValue(value);
    }

    default String map(Cliente.GeneroEnum value) {
        return value == null ? null : value.getValue();
    }

    default ClienteCreate.GeneroEnum map2(String value) {
        return value == null ? null : ClienteCreate.GeneroEnum.fromValue(value);
    }

    default String map(ClienteCreate.GeneroEnum value) {
        return value == null ? null : value.getValue();
    }

    default ClienteUpdate.GeneroEnum map3(String value) {
        return value == null ? null : ClienteUpdate.GeneroEnum.fromValue(value);
    }

    default String map(ClienteUpdate.GeneroEnum value) {
        return value == null ? null : value.getValue();
    }
}
