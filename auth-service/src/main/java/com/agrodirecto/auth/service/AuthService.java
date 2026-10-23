package com.agrodirecto.auth.service;

import com.agrodirecto.auth.dto.AuthResponse;
import com.agrodirecto.auth.dto.LoginRequest;
import com.agrodirecto.auth.dto.RegistroRequest;
import com.agrodirecto.auth.dto.UsuarioDto;
import com.agrodirecto.auth.entity.Rol;
import com.agrodirecto.auth.entity.Usuario;
import com.agrodirecto.auth.repository.UsuarioRepository;
import com.agrodirecto.common.exception.BusinessException;
import com.agrodirecto.common.security.AuthUser;
import com.agrodirecto.common.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public UsuarioDto registrar(RegistroRequest r) {
        Rol rol;
        try {
            rol = Rol.valueOf(r.rol().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El perfil debe ser CLIENTE o AGRICULTOR.");
        }
        if (rol == Rol.ADMINISTRADOR) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No se puede crear un administrador desde el registro.");
        }
        String email = r.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }
        Usuario u = new Usuario();
        u.setNombre(r.nombre().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRol(rol);
        u.setTelefono(r.telefono());
        u.setDireccion(r.direccion());
        u.setDistrito(r.distrito());
        u.setAprobado(rol == Rol.CLIENTE); // los agricultores esperan aprobación del administrador
        return UsuarioDto.de(usuarios.save(u));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        Usuario u = usuarios.findByEmailIgnoreCase(r.email().trim())
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));
        if (!u.isActivo()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Tu cuenta está desactivada. Contacta al administrador.");
        }
        String token = jwt.generar(new AuthUser(u.getId(), u.getEmail(), u.getNombre(), u.getRol().name()));
        return new AuthResponse(token, jwt.getExpiracionMinutos(), UsuarioDto.de(u));
    }

    @Transactional(readOnly = true)
    public UsuarioDto perfil(Long id) {
        return usuarios.findById(id).map(UsuarioDto::de)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
    }
}
