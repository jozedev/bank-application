package com.jozedev.bankapp.account.mapper;

import org.mapstruct.Mapper;

import com.jozedev.bankapp.account.dto.Cuenta;
import com.jozedev.bankapp.account.dto.CuentaCreate;
import com.jozedev.bankapp.account.dto.CuentaPartialUpdate;
import com.jozedev.bankapp.account.model.Account;

@Mapper(config = GlobalMapperConfig.class)
public interface AccountMapper {
    Cuenta toDto(Account account);
    Account toEntity(Cuenta cuenta);
    Account toEntity(CuentaCreate cuenta);
    Account toEntity(CuentaPartialUpdate cuenta);
}
