package com.jozedev.bankapp.account.service.impl;

import com.jozedev.bankapp.account.dto.Cuenta;
import com.jozedev.bankapp.account.dto.Movimiento;
import com.jozedev.bankapp.account.dto.MovimientoCreate;
import com.jozedev.bankapp.account.dto.MovimientoReporte;
import com.jozedev.bankapp.account.exception.NotActiveException;
import com.jozedev.bankapp.account.exception.NotFoundException;
import com.jozedev.bankapp.account.exception.NotEnoughFundsException;
import com.jozedev.bankapp.account.exception.QuotaExceededException;
import com.jozedev.bankapp.account.mapper.TransactionMapper;
import com.jozedev.bankapp.account.model.Transaction;
import com.jozedev.bankapp.account.model.dto.ClientDto;
import com.jozedev.bankapp.account.repository.TransactionRepository;
import com.jozedev.bankapp.account.service.AccountService;
import com.jozedev.bankapp.account.service.PdfReportService;
import com.jozedev.bankapp.account.service.ServiceClient;
import com.jozedev.bankapp.account.util.ServiceKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private ServiceClient serviceClient;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private PdfReportService pdfReportService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction storedTransaction;
    private Movimiento movimientoDto;
    private Cuenta cuenta;
    private ClientDto clientDto;

    @BeforeEach
    void setUp() {
        storedTransaction = new Transaction();
        storedTransaction.setId(1L);
        storedTransaction.setFecha(OffsetDateTime.now());
        storedTransaction.setTipo("DEBITO");
        storedTransaction.setValor(-200.0);
        storedTransaction.setBalance(800.0);
        storedTransaction.setNumeroCuenta(100L);

        movimientoDto = new Movimiento();
        movimientoDto.setId(1L);
        movimientoDto.setValor(-200.0);
        movimientoDto.setBalance(800.0);
        movimientoDto.setNumeroCuenta(100L);

        cuenta = new Cuenta();
        cuenta.setNumeroCuenta(100L);
        cuenta.setSaldoInicial(1000.0);
        cuenta.setEstado(true);
        cuenta.setClienteId(1L);
        cuenta.setTipo("AHORROS");

        clientDto = ClientDto.builder()
                .nombre("Juan Perez")
                .clienteId(1L)
                .estado(true)
                .build();
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllTransactions() {
        when(transactionRepository.findAll()).thenReturn(Flux.just(storedTransaction));
        when(transactionMapper.toDto(storedTransaction)).thenReturn(movimientoDto);

        StepVerifier.create(transactionService.findAll())
                .expectNext(movimientoDto)
                .verifyComplete();
    }

    @Test
    void findAll_returnsEmpty_whenNoTransactionsExist() {
        when(transactionRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(transactionService.findAll())
                .verifyComplete();
    }

    // ─── findTransactionById ─────────────────────────────────────────────────

    @Test
    void findTransactionById_returnsTransaction_whenFound() {
        when(transactionRepository.findById(1L)).thenReturn(Mono.just(storedTransaction));
        when(transactionMapper.toDto(storedTransaction)).thenReturn(movimientoDto);

        StepVerifier.create(transactionService.findTransactionById(1L))
                .expectNext(movimientoDto)
                .verifyComplete();
    }

    @Test
    void findTransactionById_throwsNotFoundException_whenNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(transactionService.findTransactionById(99L))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(NotFoundException.class);
                    assertThat(e.getMessage()).contains("99");
                })
                .verify();
    }

    // ─── findLatestTransaction ───────────────────────────────────────────────

    @Test
    void findLatestTransaction_returnsMostRecentTransaction() {
        when(transactionRepository.findLastTransaction(100L)).thenReturn(Mono.just(storedTransaction));
        when(transactionMapper.toDto(storedTransaction)).thenReturn(movimientoDto);

        StepVerifier.create(transactionService.findLatestTransaction(100L))
                .expectNext(movimientoDto)
                .verifyComplete();
    }

    // ─── saveTransaction ─────────────────────────────────────────────────────

    @Test
    void saveTransaction_deposit_calculatesNewBalanceAndSaves() {
        Transaction incomingTx = buildTransaction(100L, "CREDITO", 500.0, OffsetDateTime.now(), 0.0);
        Transaction savedTx = buildTransaction(100L, "CREDITO", 500.0, OffsetDateTime.now(), 1300.0);
        savedTx.setId(2L);

        MovimientoCreate input = new MovimientoCreate(OffsetDateTime.now(), "CREDITO", 500.0, 100L);
        Movimiento expectedResult = new Movimiento();
        expectedResult.setBalance(1300.0);

        when(transactionMapper.toEntity(input)).thenReturn(incomingTx);
        when(accountService.findAccountByNumber(100L)).thenReturn(Mono.just(cuenta));
        when(transactionRepository.findLastTransaction(100L)).thenReturn(Mono.just(storedTransaction)); // balance=800
        when(transactionRepository.getTranasctionsFromDays(eq(100L), any(), any())).thenReturn(Flux.empty());
        when(transactionRepository.save(incomingTx)).thenReturn(Mono.just(savedTx));
        when(transactionMapper.toDto(savedTx)).thenReturn(expectedResult);

        StepVerifier.create(transactionService.saveTransaction(Mono.just(input)))
                .expectNext(expectedResult)
                .verifyComplete();

        verify(transactionRepository).save(incomingTx);
        assertThat(incomingTx.getBalance()).isEqualTo(1300.0);
    }

    @Test
    void saveTransaction_withdrawal_withinDailyQuota_savesSuccessfully() {
        // Previous withdrawals today: -200. New withdrawal: -300. Total abs = 500 <= 1000.
        Transaction previousWithdrawal = buildTransaction(100L, "DEBITO", -200.0, OffsetDateTime.now(), 1800.0);
        Transaction incomingTx = buildTransaction(100L, "DEBITO", -300.0, OffsetDateTime.now(), 0.0);
        Transaction lastStoredTx = buildTransaction(100L, "DEBITO", -200.0, OffsetDateTime.now(), 2000.0);
        Transaction savedTx = buildTransaction(100L, "DEBITO", -300.0, OffsetDateTime.now(), 1700.0);
        savedTx.setId(3L);

        MovimientoCreate input = new MovimientoCreate(OffsetDateTime.now(), "DEBITO", -300.0, 100L);
        Movimiento expectedResult = new Movimiento();
        expectedResult.setBalance(1700.0);

        when(transactionMapper.toEntity(input)).thenReturn(incomingTx);
        when(accountService.findAccountByNumber(100L)).thenReturn(Mono.just(cuenta));
        when(transactionRepository.findLastTransaction(100L)).thenReturn(Mono.just(lastStoredTx)); // balance=2000
        when(transactionRepository.getTranasctionsFromDays(eq(100L), any(), any()))
                .thenReturn(Flux.just(previousWithdrawal));
        when(transactionRepository.save(incomingTx)).thenReturn(Mono.just(savedTx));
        when(transactionMapper.toDto(savedTx)).thenReturn(expectedResult);

        StepVerifier.create(transactionService.saveTransaction(Mono.just(input)))
                .expectNext(expectedResult)
                .verifyComplete();
    }

    @Test
    void saveTransaction_throwsNotFoundException_whenAccountDoesNotExist() {
        Transaction incomingTx = buildTransaction(99L, "CREDITO", 100.0, OffsetDateTime.now(), 0.0);
        MovimientoCreate input = new MovimientoCreate(OffsetDateTime.now(), "CREDITO", 100.0, 99L);

        when(transactionMapper.toEntity(input)).thenReturn(incomingTx);
        when(accountService.findAccountByNumber(99L)).thenReturn(Mono.empty());
        when(transactionRepository.findLastTransaction(99L)).thenReturn(Mono.just(storedTransaction));
        when(transactionRepository.getTranasctionsFromDays(eq(99L), any(), any())).thenReturn(Flux.empty());

        StepVerifier.create(transactionService.saveTransaction(Mono.just(input)))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(NotFoundException.class);
                    assertThat(e.getMessage()).contains("99");
                })
                .verify();
    }

    @Test
    void saveTransaction_throwsNotActiveException_whenAccountIsInactive() {
        cuenta.setEstado(false);
        Transaction incomingTx = buildTransaction(100L, "CREDITO", 100.0, OffsetDateTime.now(), 0.0);
        MovimientoCreate input = new MovimientoCreate(OffsetDateTime.now(), "CREDITO", 100.0, 100L);

        when(transactionMapper.toEntity(input)).thenReturn(incomingTx);
        when(accountService.findAccountByNumber(100L)).thenReturn(Mono.just(cuenta));
        when(transactionRepository.findLastTransaction(100L)).thenReturn(Mono.just(storedTransaction));
        when(transactionRepository.getTranasctionsFromDays(eq(100L), any(), any())).thenReturn(Flux.empty());

        StepVerifier.create(transactionService.saveTransaction(Mono.just(input)))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(NotActiveException.class);
                    assertThat(e.getMessage()).contains("100");
                })
                .verify();
    }

    @Test
    void saveTransaction_throwsNotEnoughFundsException_whenWithdrawalExceedsBalance() {
        // lastBalance=800, withdrawal=-2000 → newBalance=-1200 < 0
        Transaction incomingTx = buildTransaction(100L, "DEBITO", -2000.0, OffsetDateTime.now(), 0.0);
        MovimientoCreate input = new MovimientoCreate(OffsetDateTime.now(), "DEBITO", -2000.0, 100L);

        when(transactionMapper.toEntity(input)).thenReturn(incomingTx);
        when(accountService.findAccountByNumber(100L)).thenReturn(Mono.just(cuenta));
        when(transactionRepository.findLastTransaction(100L)).thenReturn(Mono.just(storedTransaction)); // balance=800
        when(transactionRepository.getTranasctionsFromDays(eq(100L), any(), any())).thenReturn(Flux.empty());

        StepVerifier.create(transactionService.saveTransaction(Mono.just(input)))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(NotEnoughFundsException.class))
                .verify();
    }

    @Test
    void saveTransaction_throwsQuotaExceededException_whenDailyWithdrawalLimitExceeded() {
        // Previous withdrawals today: -800. New withdrawal: -400. Total abs = 1200 > 1000.
        // lastBalance=1600 → newBalance=1600-400=1200 (no NotEnoughFunds)
        Transaction previousWithdrawal = buildTransaction(100L, "DEBITO", -800.0, OffsetDateTime.now(), 800.0);
        Transaction lastStoredTx = buildTransaction(100L, "DEPOSITO", 1600.0, OffsetDateTime.now(), 1600.0);
        Transaction incomingTx = buildTransaction(100L, "DEBITO", -400.0, OffsetDateTime.now(), 0.0);
        MovimientoCreate input = new MovimientoCreate(OffsetDateTime.now(), "DEBITO", -400.0, 100L);

        when(transactionMapper.toEntity(input)).thenReturn(incomingTx);
        when(accountService.findAccountByNumber(100L)).thenReturn(Mono.just(cuenta));
        when(transactionRepository.findLastTransaction(100L)).thenReturn(Mono.just(lastStoredTx)); // balance=1600
        when(transactionRepository.getTranasctionsFromDays(eq(100L), any(), any()))
                .thenReturn(Flux.just(previousWithdrawal));

        StepVerifier.create(transactionService.saveTransaction(Mono.just(input)))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(QuotaExceededException.class))
                .verify();
    }

    // ─── deleteTransaction ───────────────────────────────────────────────────

    @Test
    void deleteTransaction_completesSuccessfully_whenTransactionExists() {
        when(transactionRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(transactionRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(transactionService.deleteTransaction(1L))
                .verifyComplete();

        verify(transactionRepository).deleteById(1L);
    }

    @Test
    void deleteTransaction_throwsNotFoundException_whenTransactionDoesNotExist() {
        when(transactionRepository.existsById(99L)).thenReturn(Mono.just(false));

        StepVerifier.create(transactionService.deleteTransaction(99L))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(NotFoundException.class);
                    assertThat(e.getMessage()).contains("99");
                })
                .verify();
    }

    // ─── getReportOfClient ───────────────────────────────────────────────────

    @Test
    void getReportOfClient_returnsReport_whenClientHasAccounts() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(30);
        OffsetDateTime end = OffsetDateTime.now();
        MovimientoReporte movimientoReporte = new MovimientoReporte();

        when(accountService.findByClientId(1L)).thenReturn(Flux.just(cuenta));
        when(transactionRepository.findLastTransaction(100L)).thenReturn(Mono.just(storedTransaction));
        when(serviceClient.getForEntity(eq(ServiceKeys.CLIENT_INFO), eq(ClientDto.class), eq(1L)))
                .thenReturn(Mono.just(clientDto));
        when(transactionRepository.getTranasctionsFromDays(eq(100L), eq(start), eq(end)))
                .thenReturn(Flux.just(storedTransaction));
        when(transactionMapper.toReportDto(storedTransaction)).thenReturn(movimientoReporte);

        StepVerifier.create(transactionService.getReportOfClient(1L, start, end))
                .assertNext(report -> {
                    assertThat(report.getCliente()).isEqualTo("Juan Perez");
                    assertThat(report.getNumeroCuenta()).isEqualTo(100L);
                    assertThat(report.getSaldo()).isEqualTo(800.0); // last transaction balance
                    assertThat(report.getMovimiento()).hasSize(1);
                })
                .verifyComplete();
    }

    @Test
    void getReportOfClient_usesSaldoInicial_whenNoTransactionsExist() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(30);
        OffsetDateTime end = OffsetDateTime.now();

        when(accountService.findByClientId(1L)).thenReturn(Flux.just(cuenta));
        when(transactionRepository.findLastTransaction(100L)).thenReturn(Mono.empty()); // no transactions
        when(serviceClient.getForEntity(eq(ServiceKeys.CLIENT_INFO), eq(ClientDto.class), eq(1L)))
                .thenReturn(Mono.just(clientDto));
        when(transactionRepository.getTranasctionsFromDays(eq(100L), eq(start), eq(end)))
                .thenReturn(Flux.empty());

        StepVerifier.create(transactionService.getReportOfClient(1L, start, end))
                .assertNext(report -> assertThat(report.getSaldo()).isEqualTo(1000.0)) // saldoInicial
                .verifyComplete();
    }

    @Test
    void getReportOfClient_throwsNotFoundException_whenClientHasNoAccounts() {
        when(accountService.findByClientId(99L)).thenReturn(Flux.empty());

        StepVerifier.create(transactionService.getReportOfClient(
                        99L,
                        OffsetDateTime.now().minusDays(30),
                        OffsetDateTime.now()))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(NotFoundException.class);
                    assertThat(e.getMessage()).contains("99");
                })
                .verify();
    }

    // ─── generatePdfReportOfClient ───────────────────────────────────────────

    @Test
    void generatePdfReportOfClient_returnsPdfBytes_whenReportIsGenerated() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(30);
        OffsetDateTime end = OffsetDateTime.now();
        byte[] expectedPdf = new byte[]{1, 2, 3};

        when(accountService.findByClientId(1L)).thenReturn(Flux.just(cuenta));
        when(transactionRepository.findLastTransaction(100L)).thenReturn(Mono.just(storedTransaction));
        when(serviceClient.getForEntity(eq(ServiceKeys.CLIENT_INFO), eq(ClientDto.class), eq(1L)))
                .thenReturn(Mono.just(clientDto));
        when(transactionRepository.getTranasctionsFromDays(eq(100L), eq(start), eq(end)))
                .thenReturn(Flux.empty());
        when(pdfReportService.generate(any())).thenReturn(expectedPdf);

        StepVerifier.create(transactionService.generatePdfReportOfClient(1L, start, end))
                .expectNext(expectedPdf)
                .verifyComplete();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Transaction buildTransaction(Long accountNumber, String type, double value,
                                         OffsetDateTime fecha, double balance) {
        Transaction tx = new Transaction();
        tx.setNumeroCuenta(accountNumber);
        tx.setTipo(type);
        tx.setValor(value);
        tx.setFecha(fecha);
        tx.setBalance(balance);
        return tx;
    }
}
