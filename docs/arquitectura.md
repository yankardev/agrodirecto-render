# Arquitectura

```mermaid
flowchart LR
  U[Navegador] -->|HTTP| G[api-gateway :8080]
  G --> A[auth-service :8081]
  G --> C[catalogo-service :8082]
  G --> P[pedido-service :8083]
  A --> DA[(AgroAuthDB)]
  C --> DC[(AgroCatalogoDB)]
  P --> DP[(AgroPedidoDB)]
  P -. /internal stock .-> C
  A -. /internal habilitación .-> C
  C -. /internal aprobado .-> A
```

## Modelo de datos (una base por servicio, sin claves foráneas entre bases)

```mermaid
erDiagram
  USUARIO { long id PK string email string rol bool aprobado bool activo }
  DISTRITO { long id PK string nombre }
  CATEGORIA ||--o{ PRODUCTO : clasifica
  UNIDAD_MEDIDA ||--o{ PRODUCTO : mide
  PRODUCTO { long id PK long agricultor_id decimal precio int stock bool habilitado }
  COMPRA ||--|{ PEDIDO : "se divide en"
  PEDIDO ||--|{ DETALLE_PEDIDO : contiene
  CARRITO_ITEM { long cliente_id long producto_id int cantidad }
```
`PRODUCTO.agricultor_id`, `COMPRA.cliente_id` y `PEDIDO.agricultor_id` referencian a `USUARIO` (otro servicio) solo por ID.

## Decisiones

| Decisión | Motivo |
|---|---|
| 3 servicios + gateway | Separa seguridad, inventario y ventas, sin fragmentar de más para un equipo pequeño |
| Una base por servicio | Cada servicio es dueño de sus datos |
| JWT firmado compartido | Cada servicio valida el token sin llamar al de autenticación |
| Reserva de stock con bloqueo pesimista | Evita vender el mismo stock dos veces con compras simultáneas |
| SSE en lugar de SignalR | Avisos en tiempo real sin dependencias adicionales |
