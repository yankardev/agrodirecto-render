package com.agrodirecto.catalogo.repository;

import com.agrodirecto.catalogo.entity.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    List<Producto> findByAgricultorIdOrderByNombreAsc(Long agricultorId);

    /** Bloquea la fila mientras dura la transacción: evita que dos compras descuenten el mismo stock a la vez. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> buscarConBloqueo(@Param("id") Long id);

    @Modifying
    @Query("update Producto p set p.habilitado = :habilitado where p.agricultorId = :agricultorId")
    int actualizarHabilitacion(@Param("agricultorId") Long agricultorId, @Param("habilitado") boolean habilitado);
}
