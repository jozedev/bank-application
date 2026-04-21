package com.jozedev.bankapp.account;

import com.jozedev.bankapp.account.dto.Cuenta;
import com.jozedev.bankapp.account.dto.Movimiento;
import com.jozedev.bankapp.account.model.dto.ClientDto;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.OffsetDateTime;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegrationTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTests.class);

	private static final int ACCOUNT_PORT = 8081;
	private static final int CLIENT_PORT = 8080;
	private static final int DB_PORT = 3306;

	private final RestTemplate restTemplate = new RestTemplateBuilder().build();

	private static String ACCOUNT_URL;
	private static String CLIENT_URL;

	private static ClientDto createdClient;
	private static Cuenta createdAccount;
	private static Movimiento createdTransaction;

	@ClassRule
	static ComposeContainer environment =
			new ComposeContainer(new File("../docker-compose.test.yaml"))
					.withExposedService("account", ACCOUNT_PORT, Wait.forListeningPort())
					.withExposedService("client", CLIENT_PORT, Wait.forListeningPort())
					.withExposedService("db", DB_PORT, Wait.forHealthcheck())
					.withLogConsumer("account", new Slf4jLogConsumer(LOGGER))
					.withLogConsumer("client", new Slf4jLogConsumer(LOGGER))
					.withLogConsumer("db", new Slf4jLogConsumer(LOGGER));

	@BeforeAll
	static void setUp() {
		environment.start();

		ACCOUNT_URL = "http://" + environment.getServiceHost("account", ACCOUNT_PORT)
				+ ":" +
				environment.getServicePort("account", ACCOUNT_PORT);

		CLIENT_URL = "http://" + environment.getServiceHost("client", CLIENT_PORT)
				+ ":" +
				environment.getServicePort("client", CLIENT_PORT);
	}

	@Test
	@Order(1)
	void testCreateClient() {
		createdClient = restTemplate
				.postForEntity(CLIENT_URL + "/api/clientes", getClientDto(), ClientDto.class)
				.getBody();
		LOGGER.info("Created client: {}", createdClient);
	}

	@Test
	@Order(2)
	void testCreateAccount() {
		// Crear cuenta
		createdAccount = restTemplate
				.postForEntity(ACCOUNT_URL + "/api/cuentas", getAccountDto(createdClient.getClienteId()), Cuenta.class)
				.getBody();
		LOGGER.info("Created account: {}", createdAccount);
	}

	@Test
	@Order(3)
	void testCreateTransaction() {
		// Crear cuenta
		createdTransaction = restTemplate
				.postForEntity(ACCOUNT_URL + "/api/movimientos", getTransactionDto(createdAccount.getNumeroCuenta()), Movimiento.class)
				.getBody();
		LOGGER.info("Created transaction: {}", createdTransaction);
	}

	private ClientDto getClientDto() {
		return ClientDto.builder()
				.identificacion("11111")
				.nombre("Ana Rodriguez")
				.genero("F")
				.edad(20)
				.direccion("Av 123")
				.telefono("999222999")
				.contrasena("ashcbsdhcdsj")
				.estado(true)
				.build();
	}

	private Cuenta getAccountDto(Long clientId) {
        Cuenta account = new Cuenta();
        account.setTipo("SAVINGS");
        account.setSaldoInicial(1000.0);
        account.setEstado(true);
        account.setClienteId(clientId);
        return account;
	}

	private Movimiento getTransactionDto(Long accountNumber) {
        Movimiento transaction = new Movimiento();
        transaction.setFecha(OffsetDateTime.now());
        transaction.setTipo("DEBITO");
        transaction.setValor(100.0);
        transaction.setNumeroCuenta(accountNumber);
        return transaction;
	}
}
