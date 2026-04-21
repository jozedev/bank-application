package com.jozedev.bankapp.account.repository;

import com.jozedev.bankapp.account.model.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface AccountRepository extends R2dbcRepository<Account, Long> {

    @Query("SElECT a FROM Account a WHERE a.clienteId = :clienteId")
    Flux<Account> findByClienteId(Long clienteId);

}
