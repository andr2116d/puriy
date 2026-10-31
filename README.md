# Puriy

Plataforma web para la venta y gestión de pasajes de transporte interprovincial en bus, desplegada en AWS.

## Estructura

| Carpeta | Contenido |
|---|---|
| `backend/` | API y procesos de fondo en Java 25 + Spring Boot 4; funciones Lambda |
| `frontend/` | Aplicación web |
| `infra/` | Infraestructura como código (Terraform) |
| `pruebas-carga/` | Escenarios de carga (k6) |

## Backend

Requisitos: JDK 21 o superior para ejecutar Gradle (el JDK 25 del proyecto se descarga solo) y Docker encendido. Docker se usa también al compilar: el código de jOOQ se genera aplicando las migraciones sobre un PostgreSQL temporal.

```bash
cd backend
./gradlew bootRun           # levanta PostgreSQL con Docker Compose y la API
./gradlew test              # pruebas rápidas
./gradlew integrationTest   # pruebas contra PostgreSQL real
./gradlew check             # formato, análisis estático y todas las pruebas
./gradlew spotlessApply     # aplica el formato
./gradlew assemble          # compila y arma el ZIP de cada Lambda
```

Con la API levantada, el contrato está en `http://localhost:8080/swagger-ui.html`.

Cada Lambda queda en `backend/funciones/<nombre>/build/distributions/<nombre>.zip`, para el runtime `java25`.

## Frontend

Requisitos: Node 22 o superior y pnpm.

```bash
cd frontend
pnpm install
pnpm dev     # servidor de desarrollo en http://localhost:5173
pnpm check   # lint, tipos, pruebas y build
```
