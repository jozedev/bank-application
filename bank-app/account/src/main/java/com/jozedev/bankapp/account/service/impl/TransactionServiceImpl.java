package com.jozedev.bankapp.account.service.impl;

import com.jozedev.bankapp.account.dto.Cuenta;
import com.jozedev.bankapp.account.dto.Movimiento;
import com.jozedev.bankapp.account.dto.MovimientoCreate;
import com.jozedev.bankapp.account.dto.MovimientoReporte;
import com.jozedev.bankapp.account.dto.ReporteEstadoCuenta;
import com.jozedev.bankapp.account.exception.NotActiveException;
import com.jozedev.bankapp.account.exception.NotFoundException;
import com.jozedev.bankapp.account.exception.QuotaExceededException;
import com.jozedev.bankapp.account.mapper.TransactionMapper;
import com.jozedev.bankapp.account.exception.NotEnoughFundsException;
import com.jozedev.bankapp.account.model.Transaction;
import com.jozedev.bankapp.account.model.dto.ClientDto;
import com.jozedev.bankapp.account.repository.TransactionRepository;
import com.jozedev.bankapp.account.service.AccountService;
import com.jozedev.bankapp.account.service.PdfReportService;
import com.jozedev.bankapp.account.service.ServiceClient;
import com.jozedev.bankapp.account.service.TransactionService;
import com.jozedev.bankapp.account.util.Messages;
import com.jozedev.bankapp.account.util.ServiceKeys;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final ServiceClient serviceClient;
    private final TransactionMapper transactionMapper;
    private final PdfReportService pdfReportService;

    @Override
    public Flux<Movimiento> findAll() {
        return transactionRepository.findAll().map(transactionMapper::toDto);
    }

    @Override
    public Mono<Movimiento> findTransactionById(Long id) {
        return transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException(Messages.TRANSACTION_NOT_FOUND, id)))
            .map(transactionMapper::toDto);
    }

    @Override
    public Mono<Movimiento> findLatestTransaction(Long accountNumber) {
        return transactionRepository.findLastTransaction(accountNumber).map(transactionMapper::toDto);
    }

    @Override
    public Flux<ReporteEstadoCuenta> getReportOfClient(Long clientId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return accountService.findByClientId(clientId)
            .switchIfEmpty(Mono.error(new NotFoundException(Messages.ACCOUNTS_NOT_FOUND, clientId)))
            .flatMap(account -> getReportForAccount(account, startDate, endDate));
    }

    @Override
    public Mono<byte[]> generatePdfReportOfClient(Long clientId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return getReportOfClient(clientId, startDate, endDate)
                .collectList()
                .flatMap(reports -> Mono.fromCallable(() -> pdfReportService.generate(reports))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private Mono<ReporteEstadoCuenta> getReportForAccount(Cuenta account, OffsetDateTime startDate, OffsetDateTime endDate) {
        Mono<Double> latestBalanceMono = getLatestBalance(account);
        Mono<ClientDto> clientDtoMono = serviceClient
            .getForEntity(ServiceKeys.CLIENT_INFO, ClientDto.class, account.getClienteId());
        Flux<MovimientoReporte> transactionsFlux =
                transactionRepository
                        .getTranasctionsFromDays(account.getNumeroCuenta(), startDate, endDate)
                        .map(transactionMapper::toReportDto);

        return Mono.zip(
                latestBalanceMono,
                transactionsFlux.collectList(),
                clientDtoMono
        ).map(tuple -> {
                Double latestBalance = tuple.getT1();
                List<MovimientoReporte> transactions = tuple.getT2();
                ClientDto clientDto = tuple.getT3();

                ReporteEstadoCuenta report = new ReporteEstadoCuenta();
                report.setCliente(clientDto.getNombre());
                report.setFecha(OffsetDateTime.now());
                report.setNumeroCuenta(account.getNumeroCuenta());
                report.setTipo(account.getTipo());
                report.setSaldo(latestBalance);
                report.setEstado(account.getEstado());
                report.setMovimiento(transactions);
                return report;
            }
        );
    }

    private Mono<Double> getLatestBalance(Cuenta account) {
        return transactionRepository.findLastTransaction(account.getNumeroCuenta())
                .map(Transaction::getBalance)
                .defaultIfEmpty(account.getSaldoInicial());
    }

    @Override
    @Transactional
    public Mono<Movimiento> saveTransaction(Mono<MovimientoCreate> movimientoCreate) {
        return movimientoCreate
                .map(transactionMapper::toEntity)
                .flatMap(this::validateAndCalculateBalance)
                .flatMap(transactionRepository::save)
                .map(transactionMapper::toDto);
    }

    private Mono<Transaction> validateAndCalculateBalance(Transaction transaction) {
        Mono<Cuenta> accountMono = accountService.findAccountByNumber(transaction.getNumeroCuenta())
                .switchIfEmpty(Mono.error(new NotFoundException(
                        Messages.ACCOUNT_NOT_FOUND, transaction.getNumeroCuenta()
                )));
        Mono<Double> lastBalanceMono = transactionRepository.findLastTransaction(transaction.getNumeroCuenta())
                .map(Transaction::getBalance);
        Flux<Transaction> dayWithdrawals = transactionRepository.getTranasctionsFromDays(
                transaction.getNumeroCuenta(),
                transaction.getFecha().withHour(0).withMinute(0).withSecond(0).withNano(0),
                transaction.getFecha().withHour(23).withMinute(59).withSecond(59).withNano(999999999)
        ).filter(t -> t.getValor() < 0);

        return Mono.zip(accountMono, lastBalanceMono, dayWithdrawals.collectList())
                .flatMap(tuple -> {
                    Cuenta account = tuple.getT1();
                    Double lastBalance = tuple.getT2();
                    List<Transaction> withdrawals = tuple.getT3();

                    if (Boolean.FALSE.equals(account.getEstado())) {
                        return Mono.error(new NotActiveException(
                                Messages.ACCOUNT_NOT_ACTIVE, account.getNumeroCuenta()
                        ));
                    }
                    
                    BigDecimal newBalance = BigDecimal.valueOf(lastBalance).add(BigDecimal.valueOf(transaction.getValor()));
                    if (newBalance.doubleValue() < 0) {
                        return Mono.error(
                                new NotEnoughFundsException(Messages.NOT_ENOUGH_FUNDS, account.getNumeroCuenta())
                        );
                    }
                    transaction.setBalance(newBalance.doubleValue());

                    if (transaction.getValor() < 0) {
                        BigDecimal totalWithdrawals = withdrawals.stream()
                                .map(Transaction::getValor)
                                .map(BigDecimal::valueOf)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .add(BigDecimal.valueOf(transaction.getValor()));
                        if (totalWithdrawals.abs().compareTo(BigDecimal.valueOf(1000)) > 0) {
                            return Mono.error(new QuotaExceededException(Messages.QUOTA_EXCEEDED));
                        }
                    }

                    return Mono.just(transaction);
                });
    }

    @Override
    @Transactional
    public Mono<Void> deleteTransaction(Long id) {
        return transactionRepository.existsById(id)
            .filter(Boolean::booleanValue)
            .switchIfEmpty(Mono.error(new NotFoundException(Messages.TRANSACTION_NOT_FOUND, id)))
            .flatMap(exists -> transactionRepository.deleteById(id));
    }
}
