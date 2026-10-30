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

Requisitos: JDK 21 o superior para ejecutar Gradle (el JDK 25 del proyecto se descarga solo) y Docker.

```bash
cd backend
./gradlew bootRun        # levanta PostgreSQL con Docker Compose y la API
./gradlew test           # pruebas rápidas
./gradlew check          # formato, análisis estático y todas las pruebas
./gradlew spotlessApply  # aplica el formato
```
