package com.jozedev.bankapp.account.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import com.jozedev.bankapp.account.dto.Movimiento;
import com.jozedev.bankapp.account.dto.MovimientoCreate;
import com.jozedev.bankapp.account.dto.ReporteEstadoCuenta;

public interface TransactionService {

    Flux<Movimiento> findAll();

    Mono<Movimiento> saveTransaction(Mono<MovimientoCreate> movimientoCreate);

    Mono<Movimiento> findTransactionById(Long id);

    Flux<ReporteEstadoCuenta> getReportOfClient(Long clientId, OffsetDateTime startDate, OffsetDateTime endDate);

    Mono<byte[]> generatePdfReportOfClient(Long clientId, OffsetDateTime startDate, OffsetDateTime endDate);

    Mono<Movimiento> findLatestTransaction(Long accountNumber);

    Mono<Void> deleteTransaction(Long id);
}
