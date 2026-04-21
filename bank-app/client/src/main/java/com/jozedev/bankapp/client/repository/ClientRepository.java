package com.jozedev.bankapp.client.repository;

import com.jozedev.bankapp.client.model.Client;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ClientRepository extends R2dbcRepository<Client, Long> {

}
