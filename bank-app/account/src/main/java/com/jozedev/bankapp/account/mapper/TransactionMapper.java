package com.jozedev.bankapp.account.mapper;

import org.mapstruct.Mapper;

import com.jozedev.bankapp.account.dto.Movimiento;
import com.jozedev.bankapp.account.dto.MovimientoCreate;
import com.jozedev.bankapp.account.dto.MovimientoReporte;
import com.jozedev.bankapp.account.model.Transaction;

@Mapper(config = GlobalMapperConfig.class)
public interface TransactionMapper {
    Movimiento toDto(Transaction transaction);
    MovimientoReporte toReportDto(Transaction transaction);
    Transaction toEntity(Movimiento movimiento);
    Transaction toEntity(MovimientoCreate movimiento);
}
