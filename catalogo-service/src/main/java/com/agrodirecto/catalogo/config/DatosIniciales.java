package com.agrodirecto.catalogo.config;

import com.agrodirecto.catalogo.entity.Categoria;
import com.agrodirecto.catalogo.entity.Producto;
import com.agrodirecto.catalogo.entity.UnidadMedida;
import com.agrodirecto.catalogo.repository.CategoriaRepository;
import com.agrodirecto.catalogo.repository.ProductoRepository;
import com.agrodirecto.catalogo.repository.UnidadMedidaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos de prueba. Los agricultores 2, 3 y 4 son los que crea auth-service al iniciar con la base vacía.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final CategoriaRepository categorias;
    private final UnidadMedidaRepository unidades;
    private final ProductoRepository productos;

    public DatosIniciales(CategoriaRepository categorias, UnidadMedidaRepository unidades, ProductoRepository productos) {
        this.categorias = categorias;
        this.unidades = unidades;
        this.productos = productos;
    }

    @Override
    public void run(String... args) {
        if (categorias.count() > 0) {
            return;
        }
        List<Categoria> cat = categorias.saveAll(List.of(
                new Categoria("Frutas", "Frutas frescas de temporada"),
                new Categoria("Verduras", "Hortalizas y hojas verdes"),
                new Categoria("Tubérculos", "Papas, camotes y raíces"),
                new Categoria("Granos y cereales", "Quinua, maíz, menestras"),
                new Categoria("Lácteos y huevos", "Productos de granja")));
        List<UnidadMedida> uni = unidades.saveAll(List.of(
                new UnidadMedida("Kilogramo", "kg"), new UnidadMedida("Unidad", "und"),
                new UnidadMedida("Docena", "doc"), new UnidadMedida("Litro", "l"),
                new UnidadMedida("Atado", "atado")));

        Categoria frutas = cat.get(0), verduras = cat.get(1), tuberculos = cat.get(2), granos = cat.get(3), lacteos = cat.get(4);
        UnidadMedida kg = uni.get(0), und = uni.get(1), doc = uni.get(2), atado = uni.get(4);

        // Juan Quispe (id 2) - Huaral
        crear(2L, "Juan Quispe", "Papa amarilla", "Papa de cáscara fina, ideal para sancochar", "3.80", 120, tuberculos, kg);
        crear(2L, "Juan Quispe", "Camote anaranjado", "Dulce y cremoso, cosecha de la semana", "2.90", 80, tuberculos, kg);
        crear(2L, "Juan Quispe", "Zanahoria", "Zanahoria crocante sin tratar", "2.50", 60, verduras, kg);
        crear(2L, "Juan Quispe", "Cebolla roja", "Cebolla de Arequipa", "3.20", 90, verduras, kg);
        crear(2L, "Juan Quispe", "Tomate", "Tomate italiano maduro", "4.50", 50, verduras, kg);
        // María Condori (id 3) - Cañete
        crear(3L, "María Condori", "Mandarina", "Mandarina sin pepa, muy jugosa", "4.20", 70, frutas, kg);
        crear(3L, "María Condori", "Palta Hass", "Palta cremosa lista para consumir", "9.00", 40, frutas, kg);
        crear(3L, "María Condori", "Plátano de seda", "Plátano dulce de la selva central", "3.50", 100, frutas, kg);
        crear(3L, "María Condori", "Fresa", "Fresa fresca en canasta", "7.50", 30, frutas, kg);
        crear(3L, "María Condori", "Lechuga", "Lechuga americana hidropónica", "1.80", 45, verduras, und);
        // Pedro Huamán (id 4) - Lurín
        crear(4L, "Pedro Huamán", "Quinua blanca", "Quinua perlada, lavada", "12.00", 55, granos, kg);
        crear(4L, "Pedro Huamán", "Maíz morado", "Para chicha y mazamorra", "6.50", 65, granos, kg);
        crear(4L, "Pedro Huamán", "Habas frescas", "Habas verdes con vaina", "5.50", 35, granos, kg);
        crear(4L, "Pedro Huamán", "Huevos de campo", "Gallinas criadas al aire libre", "9.50", 25, lacteos, doc);
        crear(4L, "Pedro Huamán", "Queso fresco", "Queso de leche entera, artesanal", "14.00", 20, lacteos, kg);
        crear(4L, "Pedro Huamán", "Culantro", "Atado grande recién cortado", "1.50", 80, verduras, atado);
    }

    private void crear(Long agricultorId, String agricultor, String nombre, String descripcion, String precio,
                       int stock, Categoria categoria, UnidadMedida unidad) {
        Producto p = new Producto();
        p.setAgricultorId(agricultorId);
        p.setAgricultorNombre(agricultor);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(new BigDecimal(precio));
        p.setStock(stock);
        p.setCategoria(categoria);
        p.setUnidad(unidad);
        productos.save(p);
    }
}
