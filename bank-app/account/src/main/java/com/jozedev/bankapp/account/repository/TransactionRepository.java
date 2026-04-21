package com.jozedev.bankapp.account.repository;

import com.jozedev.bankapp.account.model.Transaction;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface TransactionRepository extends R2dbcRepository<Transaction, Long> {

    @Query(value = "SElECT * FROM transaction t WHERE t.numero_cuenta = :numeroCuenta ORDER BY t.id DESC LIMIT 1")
    Mono<Transaction> findLastTransaction(Long numeroCuenta);

    @Query(value = """
            SELECT * FROM transaction
            WHERE numero_cuenta = :numeroCuenta
            AND fecha BETWEEN :startDate AND :endDate
            ORDER BY id DESC
        """)
    Flux<Transaction> getTranasctionsFromDays(Long numeroCuenta, OffsetDateTime startDate, OffsetDateTime endDate);
}
