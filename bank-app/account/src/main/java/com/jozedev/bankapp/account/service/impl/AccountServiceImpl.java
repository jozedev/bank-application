package com.jozedev.bankapp.account.service.impl;

import com.jozedev.bankapp.account.dto.Cuenta;
import com.jozedev.bankapp.account.dto.CuentaCreate;
import com.jozedev.bankapp.account.dto.CuentaPartialUpdate;
import com.jozedev.bankapp.account.exception.NotActiveException;
import com.jozedev.bankapp.account.exception.NotFoundException;
import com.jozedev.bankapp.account.mapper.AccountMapper;
import com.jozedev.bankapp.account.model.Account;
import com.jozedev.bankapp.account.model.dto.ClientDto;
import com.jozedev.bankapp.account.repository.AccountRepository;
import com.jozedev.bankapp.account.service.AccountService;
import com.jozedev.bankapp.account.service.ServiceClient;
import com.jozedev.bankapp.account.util.Messages;
import com.jozedev.bankapp.account.util.ServiceKeys;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ServiceClient serviceClient;
    private final AccountMapper accountMapper;

    @Override
    public Flux<Cuenta> findAll() {
        return accountRepository.findAll().map(accountMapper::toDto);
    }

    @Override
    public Flux<Cuenta> findByClientId(Long clientId) {
        return accountRepository.findByClienteId(clientId)
            .switchIfEmpty(Mono.error(new NotFoundException(Messages.ACCOUNT_NOT_FOUND, clientId)))
            .map(accountMapper::toDto);
    }

    @Override
    public Mono<Cuenta> findAccountByNumber(Long accountNumber) {
        return accountRepository.findById(accountNumber)
            .switchIfEmpty(Mono.error(new NotFoundException(Messages.ACCOUNT_NOT_FOUND, accountNumber)))
            .map(accountMapper::toDto);
    }

    
    @Override
    @Transactional
    public Mono<Cuenta> saveAccount(Mono<CuentaCreate> account) {

        return account.flatMap(this::validateClientAndSave)
              .map(accountMapper::toDto);
    }

    private Mono<Account> validateClientAndSave(CuentaCreate a) {
        return serviceClient
                .getForEntity(ServiceKeys.CLIENT_INFO, ClientDto.class, a.getClienteId())
                .switchIfEmpty(Mono.error(new NotFoundException(Messages.CLIENT_NOT_FOUND, a.getClienteId())))
                .flatMap(client -> {
                    if (Boolean.FALSE.equals(client.getEstado())) {
                        return Mono.error(
                                new NotActiveException(
                                        Messages.CLIENT_NOT_ACTIVE,
                                        a.getClienteId()
                                )
                        );
                    }
                    return accountRepository.save(accountMapper.toEntity(a));
                });
    }

    @Override
    @Transactional
    public Mono<Cuenta> updateAccount(Long accountNumber, Mono<CuentaCreate> account) {
        return account
                .flatMap(a -> this.updateAccountIfExists(accountNumber, a))
                .map(accountMapper::toDto);
    }

    private Mono<Account> updateAccountIfExists(Long accountNumber, CuentaCreate a) {
        return serviceClient
            .getForEntity(ServiceKeys.CLIENT_INFO, ClientDto.class, a.getClienteId())
            .switchIfEmpty(Mono.error(new NotFoundException(Messages.CLIENT_NOT_FOUND, a.getClienteId())))
            .then(accountRepository.findById(accountNumber)
                .switchIfEmpty(Mono.error(new NotFoundException(Messages.ACCOUNT_NOT_FOUND, accountNumber))))
            .then(Mono.defer(() -> {
                Account account = accountMapper.toEntity(a);
                account.setNumeroCuenta(accountNumber);
                return accountRepository.save(account);
            }));
    }

    @Override
    @Transactional
    public Mono<Cuenta> partialUpdate(Long accountNumber, Mono<CuentaPartialUpdate> cuentaPartialUpdate) {
        return accountRepository.findById(accountNumber)
            .switchIfEmpty(Mono.error(
                    new NotFoundException(Messages.ACCOUNT_NOT_FOUND, accountNumber)
            ))
            .flatMap(oldAccount ->
                    cuentaPartialUpdate.map(partialAccount -> {
                        oldAccount.setNumeroCuenta(accountNumber);
                        oldAccount.setEstado(partialAccount.getEstado());
                        return oldAccount;
                    })
            )
            .flatMap(accountRepository::save)
            .map(accountMapper::toDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteAccount(Long accountNumber) {
        return accountRepository.existsById(accountNumber)
            .filter(Boolean::booleanValue)
            .switchIfEmpty(Mono.error(new NotFoundException(Messages.ACCOUNT_NOT_FOUND, accountNumber)))
            .flatMap(exists -> accountRepository.deleteById(accountNumber));
    }
}
