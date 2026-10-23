package com.agrodirecto.pedido.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "carrito_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cliente_id", "producto_id"}))
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "producto_nombre", nullable = false, length = 100)
    private String productoNombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "agricultor_id", nullable = false)
    private Long agricultorId;

    @Column(name = "agricultor_nombre", nullable = false, length = 100)
    private String agricultorNombre;

    @Column(length = 10)
    private String unidad;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Long getAgricultorId() {
        return agricultorId;
    }

    public void setAgricultorId(Long agricultorId) {
        this.agricultorId = agricultorId;
    }

    public String getAgricultorNombre() {
        return agricultorNombre;
    }

    public void setAgricultorNombre(String agricultorNombre) {
        this.agricultorNombre = agricultorNombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
}
