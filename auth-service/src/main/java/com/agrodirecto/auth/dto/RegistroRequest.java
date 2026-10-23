package com.agrodirecto.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 60, message = "La contraseña debe tener al menos 6 caracteres") String password,
        @Size(max = 20) String telefono,
        @Size(max = 200) String direccion,
        @Size(max = 80) String distrito,
        @NotBlank(message = "Elige si eres cliente o agricultor") String rol) {
}
