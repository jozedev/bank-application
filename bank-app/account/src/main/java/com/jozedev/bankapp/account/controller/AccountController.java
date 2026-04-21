package com.jozedev.bankapp.account.controller;

import com.jozedev.bankapp.account.api.CuentasApi;
import com.jozedev.bankapp.account.dto.Cuenta;
import com.jozedev.bankapp.account.dto.CuentaCreate;
import com.jozedev.bankapp.account.dto.CuentaPartialUpdate;
import com.jozedev.bankapp.account.service.AccountService;

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
public class AccountController implements CuentasApi {

    private final AccountService accountService;

    @Override
    public Mono<ResponseEntity<Flux<Cuenta>>> getCuentas(final ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(accountService.findAll()));
    }

    @Override
    public Mono<ResponseEntity<Cuenta>> getCuenta(
        Long id,
        final ServerWebExchange exchange
    ) {
        return accountService.findAccountByNumber(id).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Cuenta>> createCuenta(
        @Valid @RequestBody Mono<CuentaCreate> cuentaCreate,
        final ServerWebExchange exchange
    ) {
        return accountService.saveAccount(cuentaCreate).map(ResponseEntity.status(HttpStatus.CREATED)::body);
    }

    @Override
    public Mono<ResponseEntity<Cuenta>> updateCuenta(
        Long id,
        @Valid @RequestBody Mono<CuentaCreate> cuentaCreate,
        final ServerWebExchange exchange
    ) {
        return accountService.updateAccount(id, cuentaCreate).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Cuenta>> updatePartialCuenta(
        Long id,
        @Valid @RequestBody Mono<CuentaPartialUpdate> cuentaPartialUpdate,
        final ServerWebExchange exchange
    ) {
        return accountService.partialUpdate(id, cuentaPartialUpdate).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCuenta(
        Long id,
        final ServerWebExchange exchange
    ) {
        return accountService.deleteAccount(id).map(s -> ResponseEntity.noContent().build());
    }
}
