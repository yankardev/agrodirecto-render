package com.agrodirecto.pedido.report;

import java.time.LocalDate;

/** Filtros de los reportes: rango de fechas (inclusive) y agricultor. Todos opcionales. */
public record Filtro(LocalDate desde, LocalDate hasta, Long agricultorId) {

    /** Si el rango llega al revés, se corrige en lugar de devolver un reporte vacío. */
    public Filtro normalizado() {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            return new Filtro(hasta, desde, agricultorId);
        }
        return this;
    }
}
