# AgroDirecto en Render (demo académica)

## Arquitectura
- agrodirecto-gateway-yc2026 -> único enlace que se entrega al profesor.
- agrodirecto-auth-yc2026
- agrodirecto-catalogo-yc2026
- agrodirecto-pedido-yc2026
- Perfil H2 en memoria para los 3 servicios de negocio.

## Despliegue
1. Subir este proyecto a GitHub.
2. En Render: New > Blueprint.
3. Conectar el repositorio.
4. Render detectará `render.yaml` y propondrá 4 Web Services con plan Free.
5. Aplicar el Blueprint.
6. Esperar a que los 4 servicios queden Live.
7. Abrir el hostname del servicio `agrodirecto-gateway-yc2026`.

## Antes de la exposición
Los servicios Free se duermen después de inactividad. Unos minutos antes, abrir:
- https://<auth>/actuator/health
- https://<catalogo>/actuator/health
- https://<pedido>/actuator/health
- https://<gateway>/actuator/health
Así los cuatro quedan despiertos antes de que el profesor pruebe el sistema.

## Persistencia
H2 es en memoria. Si un servicio se reinicia o se duerme, vuelve a cargar sus datos iniciales.
Es deliberado para la demo gratuita. La configuración original con SQL Server se conserva en el proyecto.
