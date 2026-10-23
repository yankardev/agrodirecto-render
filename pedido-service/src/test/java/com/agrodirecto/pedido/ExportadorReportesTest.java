package com.agrodirecto.pedido;

import com.agrodirecto.pedido.report.ExportadorReportes;
import com.agrodirecto.pedido.report.ReporteDtos.Tabla;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifica que los tres formatos de exportación generen archivos válidos. */
class ExportadorReportesTest {

    private final ExportadorReportes exportador = new ExportadorReportes();
    private final Tabla tabla = new Tabla("Prueba", List.of("Producto", "Total (S/)"),
            List.of(List.of("Papa \"amarilla\"", "38.00"), List.of("Quinua", "24.00")));

    @Test
    void pdfEmpiezaConLaFirmaDePdf() {
        byte[] pdf = exportador.pdf(tabla);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void excelEsUnZipValido() {
        byte[] xlsx = exportador.excel(tabla);
        assertEquals('P', xlsx[0]);
        assertEquals('K', xlsx[1]);
    }

    @Test
    void csvEscapaLasComillasYTieneBom() {
        String csv = new String(exportador.csv(tabla), StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\uFEFF\"Producto\",\"Total (S/)\""));
        assertTrue(csv.contains("\"Papa \"\"amarilla\"\"\",\"38.00\""));
    }

    @Test
    void unaTablaVaciaTambienSeExporta() {
        byte[] pdf = exportador.pdf(new Tabla("Vacío", List.of("A"), List.of()));
        assertTrue(pdf.length > 100);
    }
}
