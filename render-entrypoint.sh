#!/bin/sh
set -eu

# Render Free Web Services no reciben tráfico de red privada.
# Por eso, para la demo, los microservicios se llaman mediante sus hostnames públicos HTTPS.
if [ -n "${AUTH_EXTERNAL_HOSTNAME:-}" ]; then
  export AUTH_URL="https://${AUTH_EXTERNAL_HOSTNAME}"
fi
if [ -n "${CATALOGO_EXTERNAL_HOSTNAME:-}" ]; then
  export CATALOGO_URL="https://${CATALOGO_EXTERNAL_HOSTNAME}"
fi
if [ -n "${PEDIDO_EXTERNAL_HOSTNAME:-}" ]; then
  export PEDIDO_URL="https://${PEDIDO_EXTERNAL_HOSTNAME}"
fi

exec java ${JAVA_TOOL_OPTIONS:-} -jar /app/app.jar
