package com.agrodirecto.catalogo.repository;

import com.agrodirecto.catalogo.entity.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Long> {
    List<UnidadMedida> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
