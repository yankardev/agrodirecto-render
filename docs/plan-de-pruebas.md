# Plan de pruebas

| Tipo | Qué valida | Herramienta | Dónde |
|---|---|---|---|
| Unitaria | JWT (firma, expiración), estados del pedido, reserva/liberación de stock, saga del checkout, exportadores | JUnit 5 + Mockito | `*/src/test` |
| Funcional / integración | Login, 401/403 por rol, registro duplicado, arranque completo de cada servicio con H2 | Spring Boot Test + MockMvc | `AuthFlujoTest`, `*ApplicationTests` |
| Rendimiento | Login + catálogo + búsqueda + carrito bajo carga (criterio: catálogo < 1,5 s, 0 % de errores) | Apache JMeter | `tests/jmeter/agrodirecto-carga.jmx` |
| Manual (humo) | Flujo completo cliente → agricultor → administrador | Navegador | ver abajo |

## Prueba manual de humo
1. Cliente: agrega 2 productos de agricultores distintos → confirma compra → ve 2 pedidos en *Mis compras*.
2. Agricultor 1: ve su pedido (llegó aviso) → confirma → entrega.
3. Agricultor 2: cancela su pedido → el stock vuelve al catálogo.
4. Administrador: *Reportes* → aplica rango de fechas → exporta PDF, Excel y CSV.
5. Registra un agricultor nuevo → no puede publicar hasta que el administrador lo apruebe.

## Rendimiento: cómo ejecutarlo
`jmeter -n -t tests/jmeter/agrodirecto-carga.jmx -Jusuarios=50 -l resultados.jtl -e -o reporte-html`
Registra aquí tiempos promedio, percentil 90 y % de error para 20, 50 y 100 usuarios.
