package com.jozedev.bankapp.account.controller;

import com.jozedev.bankapp.account.api.MovimientosApi;
import com.jozedev.bankapp.account.dto.Movimiento;
import com.jozedev.bankapp.account.dto.MovimientoCreate;
import com.jozedev.bankapp.account.dto.ReporteEstadoCuenta;
import com.jozedev.bankapp.account.service.TransactionService;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
public class TransactionController implements MovimientosApi {

    private final TransactionService transactionService;

    @Override
    public Mono<ResponseEntity<Flux<Movimiento>>> getMovimientos(final ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(transactionService.findAll()));
    }

    @Override
    public Mono<ResponseEntity<Movimiento>> createMovimiento(
        @Valid @RequestBody Mono<MovimientoCreate> movimientoCreate,
        final ServerWebExchange exchange
    ) {
        return transactionService.saveTransaction(movimientoCreate)
                .map(ResponseEntity.status(HttpStatus.CREATED)::body);
    }

    @Override
    public Mono<ResponseEntity<Movimiento>> getMovimiento(
        Long id,
        final ServerWebExchange exchange
    ) {
        return transactionService.findTransactionById(id)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<ReporteEstadoCuenta>>> getReporteEstadoCuenta(
        Long clienteId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaFin,
        final ServerWebExchange exchange
    ) {
        return Mono.just(ResponseEntity.ok(transactionService.getReportOfClient(clienteId, fechaInicio, fechaFin)));
    }

    @Override
    public Mono<ResponseEntity<Resource>> getReporteEstadoCuentaPdf(
        Long clienteId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaFin,
        final ServerWebExchange exchange
    ) {
        return transactionService.generatePdfReportOfClient(clienteId, fechaInicio, fechaFin)
                .map(pdf -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte.pdf\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .<Resource>body(new ByteArrayResource(pdf)));
    }
}
