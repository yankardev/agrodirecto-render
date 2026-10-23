package com.agrodirecto.catalogo.service;

import com.agrodirecto.catalogo.client.AuthClient;
import com.agrodirecto.catalogo.dto.ProductoDto;
import com.agrodirecto.catalogo.dto.ProductoRequest;
import com.agrodirecto.catalogo.entity.Categoria;
import com.agrodirecto.catalogo.entity.Producto;
import com.agrodirecto.catalogo.entity.UnidadMedida;
import com.agrodirecto.catalogo.repository.CategoriaRepository;
import com.agrodirecto.catalogo.repository.ProductoRepository;
import com.agrodirecto.catalogo.repository.UnidadMedidaRepository;
import com.agrodirecto.common.dto.PageResponse;
import com.agrodirecto.common.exception.BusinessException;
import com.agrodirecto.common.security.AuthUser;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final UnidadMedidaRepository unidades;
    private final AuthClient auth;

    public ProductoService(ProductoRepository productos, CategoriaRepository categorias,
                           UnidadMedidaRepository unidades, AuthClient auth) {
        this.productos = productos;
        this.categorias = categorias;
        this.unidades = unidades;
        this.auth = auth;
    }

    /** Catálogo para clientes: solo productos activos de agricultores aprobados. */
    @Transactional(readOnly = true)
    public PageResponse<ProductoDto> catalogo(String q, Long categoriaId, int pagina, int tamano) {
        Specification<Producto> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.isTrue(root.get("activo")));
            ps.add(cb.isTrue(root.get("habilitado")));
            if (q != null && !q.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("nombre")), "%" + q.trim().toLowerCase() + "%"));
            }
            if (categoriaId != null) {
                ps.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        int p = Math.max(pagina, 0);
        int t = Math.min(Math.max(tamano, 1), 50);
        Page<Producto> resultado = productos.findAll(spec, PageRequest.of(p, t, Sort.by("nombre")));
        return PageResponse.de(resultado.getContent().stream().map(ProductoDto::de).toList(), p, t, resultado.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProductoDto obtenerVisible(Long id) {
        Producto p = productos.findById(id)
                .filter(x -> x.isActivo() && x.isHabilitado())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El producto no está disponible."));
        return ProductoDto.de(p);
    }

    @Transactional(readOnly = true)
    public List<ProductoDto> delAgricultor(Long agricultorId) {
        return productos.findByAgricultorIdOrderByNombreAsc(agricultorId).stream().map(ProductoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDto delAgricultor(Long agricultorId, Long id) {
        return ProductoDto.de(propio(agricultorId, id));
    }

    @Transactional
    public ProductoDto crear(AuthUser agricultor, ProductoRequest r) {
        if (!auth.agricultorAprobado(agricultor.id())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Tu cuenta aún no fue aprobada por el administrador. No puedes publicar productos todavía.");
        }
        Producto p = new Producto();
        p.setAgricultorId(agricultor.id());
        p.setAgricultorNombre(agricultor.nombre());
        p.setHabilitado(true);
        aplicar(p, r);
        return ProductoDto.de(productos.save(p));
    }

    @Transactional
    public ProductoDto actualizar(AuthUser agricultor, Long id, ProductoRequest r) {
        Producto p = propio(agricultor.id(), id);
        aplicar(p, r);
        return ProductoDto.de(productos.save(p));
    }

    /** "Eliminar" oculta el producto (las compras anteriores conservan su historial). */
    @Transactional
    public void ocultar(Long agricultorId, Long id) {
        Producto p = propio(agricultorId, id);
        p.setActivo(false);
        productos.save(p);
    }

    @Transactional
    public int habilitarDelAgricultor(Long agricultorId, boolean habilitado) {
        return productos.actualizarHabilitacion(agricultorId, habilitado);
    }

    // El agricultor se toma del token, nunca del formulario: nadie puede editar productos ajenos.
    private Producto propio(Long agricultorId, Long id) {
        Producto p = productos.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Producto no encontrado."));
        if (!p.getAgricultorId().equals(agricultorId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Ese producto no te pertenece.");
        }
        return p;
    }

    private void aplicar(Producto p, ProductoRequest r) {
        Categoria c = categorias.findById(r.categoriaId())
                .filter(Categoria::isActivo)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "La categoría elegida no existe o está inactiva."));
        UnidadMedida u = unidades.findById(r.unidadId())
                .filter(UnidadMedida::isActivo)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "La unidad de medida elegida no existe o está inactiva."));
        p.setNombre(r.nombre().trim());
        p.setDescripcion(r.descripcion());
        p.setPrecio(r.precio());
        p.setStock(r.stock());
        p.setCategoria(c);
        p.setUnidad(u);
        if (r.activo() != null) {
            p.setActivo(r.activo());
        }
    }
}
