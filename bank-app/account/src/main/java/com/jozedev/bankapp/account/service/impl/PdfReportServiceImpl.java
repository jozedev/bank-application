package com.jozedev.bankapp.account.service.impl;

import com.jozedev.bankapp.account.dto.MovimientoReporte;
import com.jozedev.bankapp.account.dto.ReporteEstadoCuenta;
import com.jozedev.bankapp.account.service.PdfReportService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportServiceImpl implements PdfReportService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generate(List<ReporteEstadoCuenta> reports) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("Estado de Cuenta", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            if (reports == null || reports.isEmpty()) {
                document.add(new Paragraph("No se encontraron cuentas asociadas al cliente.", normalFont));
                document.close();
                return baos.toByteArray();
            }

            for (ReporteEstadoCuenta report : reports) {
                document.add(new Paragraph("Cliente: " + report.getCliente(), boldFont));
                document.add(new Paragraph("Número de cuenta: " + report.getNumeroCuenta(), normalFont));
                document.add(new Paragraph("Tipo: " + report.getTipo(), normalFont));
                document.add(new Paragraph("Saldo: " + report.getSaldo(), normalFont));
                document.add(new Paragraph(
                        "Estado: " + (Boolean.TRUE.equals(report.getEstado()) ? "Activo" : "Inactivo"),
                        normalFont));
                document.add(new Paragraph(" "));

                List<MovimientoReporte> movimientos = report.getMovimiento();
                if (movimientos != null && !movimientos.isEmpty()) {
                    PdfPTable table = new PdfPTable(4);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{3f, 2f, 2f, 1f});
                    table.setSpacingBefore(8);
                    table.setSpacingAfter(16);

                    addTableHeader(table, boldFont);
                    for (MovimientoReporte mov : movimientos) {
                        addTableRow(table, normalFont, mov);
                    }
                    document.add(table);
                } else {
                    document.add(new Paragraph("Sin movimientos en el periodo.", normalFont));
                    document.add(new Paragraph(" "));
                }
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando el reporte PDF", e);
        }
    }

    private void addTableHeader(PdfPTable table, Font font) {
        for (String header : new String[]{"Fecha", "Tipo", "Valor", "ID"}) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(new Color(41, 128, 185));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, Font font, MovimientoReporte mov) {
        String fecha = mov.getFecha() != null ? mov.getFecha().format(FORMATTER) : "-";
        String tipo = mov.getTipo() != null ? mov.getTipo() : "-";
        String valor = mov.getValor() != null ? String.valueOf(mov.getValor()) : "-";
        String id = mov.getId() != null ? String.valueOf(mov.getId()) : "-";

        table.addCell(new PdfPCell(new Phrase(fecha, font)));
        table.addCell(new PdfPCell(new Phrase(tipo, font)));
        table.addCell(new PdfPCell(new Phrase(valor, font)));
        table.addCell(new PdfPCell(new Phrase(id, font)));
    }
}
