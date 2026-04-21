package com.jozedev.bankapp.account.model;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Transaction {

	@jakarta.persistence.Id
	@org.springframework.data.annotation.Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private OffsetDateTime fecha;
	private String tipo;
	private double valor;
	private double balance;

	@Column(name = "numero_cuenta", nullable = false)
	private Long numeroCuenta;
}
