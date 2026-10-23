package com.agrodirecto.auth.repository;

import com.agrodirecto.auth.entity.Distrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistritoRepository extends JpaRepository<Distrito, Long> {
    List<Distrito> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
