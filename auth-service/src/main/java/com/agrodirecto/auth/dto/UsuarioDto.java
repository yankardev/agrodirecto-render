package com.agrodirecto.auth.dto;

import com.agrodirecto.auth.entity.Usuario;

public record UsuarioDto(Long id, String nombre, String email, String rol, String telefono,
                         String direccion, String distrito, boolean activo, boolean aprobado) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNombre(), u.getEmail(), u.getRol().name(), u.getTelefono(),
                u.getDireccion(), u.getDistrito(), u.isActivo(), u.isAprobado());
    }
}
