package com.agrodirecto.common.contrato;

import java.math.BigDecimal;

/** Respuesta del catálogo al reservar stock: datos "congelados" del producto al momento de la compra. */
public record ItemReservado(Long productoId, String nombre, BigDecimal precio, int cantidad,
                            Long agricultorId, String agricultorNombre, String unidad) {
}
