package com.agrodirecto.auth.service;

import com.agrodirecto.auth.client.CatalogoClient;
import com.agrodirecto.auth.dto.UsuarioDto;
import com.agrodirecto.auth.entity.Rol;
import com.agrodirecto.auth.entity.Usuario;
import com.agrodirecto.auth.repository.UsuarioRepository;
import com.agrodirecto.common.exception.BusinessException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final CatalogoClient catalogo;

    public UsuarioService(UsuarioRepository usuarios, CatalogoClient catalogo) {
        this.usuarios = usuarios;
        this.catalogo = catalogo;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar(String rol, String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        return usuarios.findAll(Sort.by("nombre")).stream()
                .filter(u -> rol == null || rol.isBlank() || u.getRol().name().equalsIgnoreCase(rol))
                .filter(u -> q.isEmpty() || u.getNombre().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q))
                .map(UsuarioDto::de)
                .toList();
    }

    @Transactional
    public UsuarioDto cambiarAprobacion(Long id, boolean aprobado) {
        Usuario u = buscar(id);
        if (u.getRol() != Rol.AGRICULTOR) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Solo los agricultores requieren aprobación.");
        }
        u.setAprobado(aprobado);
        usuarios.save(u);
        catalogo.habilitarProductos(u.getId(), aprobado && u.isActivo());
        return UsuarioDto.de(u);
    }

    @Transactional
    public UsuarioDto cambiarEstado(Long id, boolean activo, Long adminActualId) {
        Usuario u = buscar(id);
        if (!activo && u.getId().equals(adminActualId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "No puedes desactivar tu propia cuenta.");
        }
        u.setActivo(activo);
        usuarios.save(u);
        if (u.getRol() == Rol.AGRICULTOR) {
            catalogo.habilitarProductos(u.getId(), activo && u.isAprobado());
        }
        return UsuarioDto.de(u);
    }

    @Transactional(readOnly = true)
    public boolean agricultorAprobado(Long id) {
        return usuarios.findById(id)
                .map(u -> u.getRol() == Rol.AGRICULTOR && u.isActivo() && u.isAprobado())
                .orElse(false);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
    }
}
