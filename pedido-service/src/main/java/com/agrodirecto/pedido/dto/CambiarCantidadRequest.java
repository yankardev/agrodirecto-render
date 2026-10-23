package com.agrodirecto.pedido.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarCantidadRequest(@NotNull(message = "Falta la cantidad") Integer cantidad) {
}
