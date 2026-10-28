# EcoAPI — Backend de Gestión de Rutas y Pesajes (EcoRecicla)

Backend desarrollado en **Java 21** con **Spring Boot**, **PostgreSQL** y **Flyway**, diseñado para soportar operaciones transaccionales seguras y sincronización offline-first con la aplicación móvil **EcoRutas**.

---

## 1. Stack Tecnológico

- **Lenguaje:** Java 21 LTS (Records para DTOs y POJOs para JPA)
- **Framework:** Spring Boot 3.4.2
- **Seguridad:** Spring Security 6 + JJWT 0.12.6 (Firma HMAC-SHA256, expiración 60 min)
- **Persistencia:** Spring Data JPA + PostgreSQL 16
- **Migraciones:** Flyway (idempotente)
- **Contenedores:** Docker & Docker Compose
- **Gestor de Compilación:** Maven (`./mvnw`)

---

## 2. Decisiones Arquitectónicas y Reglas de Negocio

### 2.1 Seguridad por Privilegios (no por strings de Roles)
Las autorizaciones en métodos y endpoints se evalúan estrictamente mediante privilegios (`hasAuthority`), tales como `PUNTO_LEER`, `PESAJE_CREAR`, `PESAJE_EDITAR`, `PESAJE_ANULAR`, `RUTA_CERRAR`, `RUTA_PUNTO_OMITIR`.

### 2.2 Reglas de Negocio Implementadas
- **RN-01 (Ruta Cerrada):** Bloqueo pesimista de fila en BD (`SELECT ... FOR UPDATE`). No se permite registrar, corregir ni anular pesajes si la ruta está `CERRADA`. Retorna `409 Conflict` (`RUTA_CERRADA`).
- **RN-02 (Corrección Propia en el Día):** 
  - Un `OPERARIO` solo puede corregir pesajes de los cuales es autor (`operario_id`) y registrados en la fecha de negocio de hoy (`America/Lima`). De lo contrario retorna `403 Forbidden` (`PESAJE_AJENO` o `PLAZO_EDICION_VENCIDO`).
  - Un `SUPERVISOR` cuenta con el privilegio `PESAJE_EDITAR_CUALQUIERA`, pudiendo corregir cualquier pesaje en una ruta abierta.
- **RN-03 (Neto Positivo):** 
  - `pesoBruto > tara` y `pesoNeto = pesoBruto - tara > 0`.
  - La API calcula autoritativamente el peso neto en el servidor y nunca confía en el valor enviado por el cliente.
  - Retorna `400 Bad Request` con el mapa `validationErrors`.
- **RN-04 (Cierre de Ruta Completo):** 
  - Una ruta solo puede cerrarse si **cada uno** de sus puntos asignados cuenta con al menos un pesaje vigente (`REGISTRADO`) o con un motivo de omisión (`motivoOmision != null`).
  - Si faltan puntos por atender u omitir, retorna `409 Conflict` (`PUNTOS_PENDIENTES`).
- **Anulación Lógica:**
  - Endpoint `POST /api/v1/pesajes/{id}/anulacion` con motivo obligatorio. No se borra físicamente de la base de datos; se actualiza `estado = ANULADO`, `motivo_anulacion`, `anulado_por` y `anulado_en`.
- **Idempotencia:**
  - Envío de `operacionId` (UUID) en la creación de pesajes. Si el backend recibe una retransmisión con el mismo `operacionId`, responde con el mismo registro sin duplicar en BD.

---

## 3. Usuarios de Demostración Semilla

| Usuario | Contraseña | Rol Asignado | Privilegios Destacados |
|---|---|---|---|
| `operario1` | `Eco2026*` | OPERARIO | Lectura, Creación, Corrección propia, Omisión |
| `supervisor1` | `Eco2026*` | SUPERVISOR | Lectura, Creación, Corrección cualquiera, Anulación, Cierre de ruta |
| `admin` | `Eco2026*` | ADMINISTRADOR | Gestión de puntos y lectura general |
| `operario2` | `Eco2026*` | OPERARIO | Usuario adicional para demostrar pesajes ajenos (RN-02) |
| `auditor1` | `Eco2026*` | AUDITOR | Solo lectura (`PUNTO_LEER`, `PESAJE_LEER`) — SC-B |

---

## 4. Instrucciones de Ejecución

### 4.1 Requisitos Previos
- Docker Desktop en ejecución.
- JDK 21 instalado (o usar la imagen Docker).

### 4.2 Levantar la Infraestructura con un solo comando
```bash
docker compose up --build
```
Esto iniciará el contenedor de base de datos PostgreSQL (`ecorecicla-db`) y compilará y ejecutará `ecorecicla-api` en el puerto `8080`.

Para desarrollo local ejecutando Spring Boot directamente:
```bash
# 1. Iniciar solo la base de datos PostgreSQL
docker compose up -d db

# 2. Compilar y ejecutar la API
./mvnw spring-boot:run
```

### 4.3 Ejecutar las Pruebas Automatizadas
```bash
./mvnw clean test
```

---

## 5. Resumen de Endpoints de la API

| Método | Endpoint | Privilegio | Código Éxito |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Público | `200 OK` |
| `GET` | `/api/v1/usuarios/me` | Autenticado | `200 OK` |
| `GET` | `/api/v1/rutas/mia?fecha=...` | `PUNTO_LEER` | `200 OK` |
| `GET` | `/api/v1/rutas/{rutaId}/puntos` | `PUNTO_LEER` | `200 OK` (Paginado) |
| `POST` | `/api/v1/rutas/{rutaId}/cierre` | `RUTA_CERRAR` | `200 OK` |
| `PUT` | `/api/v1/rutas/{rutaId}/puntos/{puntoId}/omision` | `RUTA_PUNTO_OMITIR` | `200 OK` |
| `DELETE`| `/api/v1/rutas/{rutaId}/puntos/{puntoId}/omision` | `RUTA_PUNTO_OMITIR` | `204 No Content` |
| `POST` | `/api/v1/rutas/{rutaId}/puntos/{puntoId}/pesajes` | `PESAJE_CREAR` | `201 Created` |
| `GET` | `/api/v1/puntos` | `PUNTO_LEER` | `200 OK` (Paginado) |
| `GET` | `/api/v1/puntos/{puntoId}` | `PUNTO_LEER` | `200 OK` |
| `GET` | `/api/v1/puntos/{puntoId}/pesajes` | `PESAJE_LEER` | `200 OK` (Paginado) |
| `PUT` | `/api/v1/pesajes/{pesajeId}` | `PESAJE_EDITAR` | `200 OK` |
| `POST` | `/api/v1/pesajes/{pesajeId}/anulacion` | `PESAJE_ANULAR` | `200 OK` |



---

## 6. Solicitudes de Cambio Implementadas (Parte II)

### SC-B: Rol AUDITOR de Solo Lectura
- **Rama:** `sc-b-chambilla`, **Tag:** `v1.2-sc-b`
- **Migración:** `V3__rol_auditor.sql`
- **Rol:** `AUDITOR` con privilegios `PUNTO_LEER` y `PESAJE_LEER`. Carece de privilegios de mutación (`PESAJE_CREAR`, `PESAJE_EDITAR`, `PESAJE_ANULAR`, `RUTA_CERRAR`).
- **Usuario Semilla:** `auditor1` / `Eco2026*` asignado a Ruta 1 de hoy.
- **Comportamiento:**
  - Puede consultar `GET /api/v1/rutas/mia` y `GET /api/v1/puntos/{id}/pesajes` (`200 OK`).
  - Cualquier intento de mutación (crear/corregir/anular pesaje, cerrar ruta) genera automáticamente `403 Forbidden` (`ACCESO_DENEGADO`).
  - Cobertura con prueba de integración `BackendIntegrationTests` (Test 13).

### SC-C: Porcentaje de Humedad Estimada en Pesaje
- **Rama:** `sc-c-chambilla`, **Tag:** `v1.3-sc-c`
- **Migración:** `V4__humedad_estimada.sql`
- **Columna:** `pesaje.humedad_estimada NUMERIC(5,2) NULL` con restricción de chequeo `CHECK (humedad_estimada IS NULL OR (humedad_estimada >= 0 AND humedad_estimada <= 100))`.
- **Validación:** Validado a nivel de DTO (`@DecimalMin("0.00")`, `@DecimalMax("100.00")`). Si supera el rango, responde `400 Bad Request` con `validationErrors.humedadEstimada`.
- **Cobertura:** Pruebas de integración 14 (persistencia exitosa) y 15 (rechazo fuera de rango) en `BackendIntegrationTests`.
