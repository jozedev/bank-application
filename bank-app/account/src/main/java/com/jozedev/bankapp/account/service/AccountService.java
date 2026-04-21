package com.jozedev.bankapp.account.service;

import com.jozedev.bankapp.account.dto.Cuenta;
import com.jozedev.bankapp.account.dto.CuentaCreate;
import com.jozedev.bankapp.account.dto.CuentaPartialUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Flux<Cuenta> findAll();

    Flux<Cuenta> findByClientId(Long clientId);

    Mono<Cuenta> findAccountByNumber(Long accountNumber);

    Mono<Cuenta> saveAccount(Mono<CuentaCreate> account);

    Mono<Cuenta> updateAccount(Long accountNumber, Mono<CuentaCreate> account);

    Mono<Cuenta> partialUpdate(Long accountNumber, Mono<CuentaPartialUpdate> cuentaPartialUpdate);

    Mono<Void> deleteAccount(Long accountNumber);
}
