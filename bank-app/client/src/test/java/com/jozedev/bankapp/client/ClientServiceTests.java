package com.jozedev.bankapp.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jozedev.bankapp.client.dto.Cliente;
import com.jozedev.bankapp.client.dto.Cliente.GeneroEnum;
import com.jozedev.bankapp.client.exception.ClientNotFoundException;
import com.jozedev.bankapp.client.mapper.ClientMapper;
import com.jozedev.bankapp.client.model.Client;
import com.jozedev.bankapp.client.repository.ClientRepository;
import com.jozedev.bankapp.client.service.ClientService;
import com.jozedev.bankapp.client.service.ClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ClientServiceTests {

    private final ClientRepository clientRepository = mock(ClientRepository.class);
    private final ClientMapper clienteMapper = Mappers.getMapper(ClientMapper.class);
    private final ClientService clientService = new ClientServiceImpl(clientRepository, clienteMapper);

    @Test
    void findAll_existsData_shouldReturnIdenticalList() {

        when(clientRepository.findAll()).thenReturn(Flux.fromIterable(getClientList()));

        List<Cliente> expectedResponse = getClientDtoList();
        List<Cliente> actualResponse = clientService.findAll().collectList().block();

        assertArrayEquals(new List[]{expectedResponse}, new List[]{actualResponse});
    }

    @Test
    void findById_existsData_shouldReturnClient() {

        when(clientRepository.findById(1L)).thenReturn(Mono.just(getClient()));

        Cliente expectedResponse = getClientDto();
        Cliente actualResponse = clientService.findClientById(1L).block();

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void findById_doesntExistData_throwsClientNotFoundException() {
        when(clientRepository.findById(1L)).thenReturn(Mono.empty());
        assertThrows(ClientNotFoundException.class, () -> clientService.findClientById(1L).block());
    }

    @Test
    void saveClient_correctData_returnsClient() {

        Client client = getClient();
        when(clientRepository.save(any())).thenReturn(Mono.just(client));

        Cliente expectedResponse = getClientDto();
        Cliente actualResponse = clientService.saveClient(Mono.just(clienteMapper.toDtoCreate(client))).block();

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void updateClient_existsData_returnsClient() {

        Client client = getClient();
        when(clientRepository.save(any())).thenReturn(Mono.just(client));
        when(clientRepository.findById(1L)).thenReturn(Mono.just(client));

        Cliente expectedResponse = getClientDto();
        Cliente actualResponse = clientService.updateClient(1L, Mono.just(clienteMapper.toDtoUpdate(client))).block();

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void updateClient_doesntExistData_throwsClientNotFoundException() {
        when(clientRepository.findById(1L)).thenReturn(Mono.empty());
        assertThrows(ClientNotFoundException.class, () -> 
            clientService.updateClient(1L, Mono.just(clienteMapper.toDtoUpdate(getClient()))).block());
    }

    @Test
    void deleteClient_existsData_returnsVoid() {

        when(clientRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(clientRepository.deleteById(1L)).thenReturn(Mono.empty());

        clientService.deleteClient(1L).block();
    }

    @Test
    void deleteClient_doesntExistData_throwsClientNotFoundException() {

        when(clientRepository.existsById(1L)).thenReturn(Mono.just(false));
        assertThrows(ClientNotFoundException.class, () -> 
            clientService.deleteClient(1L).block());
    }

    private Client getClient() {
        Client client = new Client();
        client.setClienteId(1L);
        client.setIdentificacion("11111");
        client.setNombre("client");
        client.setGenero("F");
        client.setEdad(20);
        client.setDireccion("Av 123");
        client.setTelefono("999222999");
        client.setEstado(true);

        return client;
    }

    private Cliente getClientDto() {
        var cliente = new Cliente()
                .clienteId(1L)
                .identificacion("11111")
                .nombre("client")
                .genero(GeneroEnum.F)
                .edad(20)
                .direccion("Av 123")
                .telefono("999222999")
                .estado(true);
        return cliente;
    }

    private List<Client> getClientList() {
        List<Client> clientsList = new ArrayList<>();
        clientsList.add(getClient());

        return clientsList;
    }

    private List<Cliente> getClientDtoList() {
        List<Cliente> clientDtoList = new ArrayList<>();
        clientDtoList.add(getClientDto());

        return clientDtoList;
    }
}
