package com.agrodirecto.pedido.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ReporteDtos {

    private ReporteDtos() {
    }

    public record Resumen(long pedidos, long unidades, BigDecimal total, BigDecimal ticketPromedio) {
    }

    public record ProductoVendido(String producto, long cantidad, BigDecimal total) {
    }

    public record VentaAgricultor(String agricultor, long pedidos, long unidades, BigDecimal total) {
    }

    public record VentaDetalle(LocalDateTime fecha, Long compraId, Long pedidoId, String producto, String agricultor,
                               int cantidad, BigDecimal precio, BigDecimal subtotal, String estado) {
    }

    /** Tabla genérica: de aquí salen el PDF, el Excel y el CSV. */
    public record Tabla(String titulo, List<String> cabeceras, List<List<String>> filas) {
    }
}
