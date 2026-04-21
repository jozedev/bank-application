package com.jozedev.bankapp.account.mapper;

import org.mapstruct.Builder;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    componentModel = "spring",
    builder = @Builder(disableBuilder = false),
    unmappedTargetPolicy = ReportingPolicy.IGNORE 
)
public interface GlobalMapperConfig {
}
