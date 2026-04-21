package com.jozedev.bankapp.account.service;

import java.util.List;

import com.jozedev.bankapp.account.dto.ReporteEstadoCuenta;

public interface PdfReportService {
    public byte[] generate(List<ReporteEstadoCuenta> reports);
}
