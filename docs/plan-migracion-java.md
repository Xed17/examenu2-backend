# Documento de Registro: Migración de Backend a Java 21

Se realizó la reimplementación completa del backend de Spring Boot Kotlin a **Java 21 LTS con Spring Boot 3.4.2**, manteniendo intactos:
1. Las 4 migraciones Flyway (V1 esquema, V2 semillas, V3 rol auditor, V4 humedad estimada).
2. La arquitectura de seguridad con JJWT y autorización granular por privilegios (`hasAuthority`).
3. El cálculo autoritativo del peso neto y las reglas de negocio RN-01 a RN-04.
4. Los DTOs inmutables implementados con Java Records nativos.
5. Los 15 casos de prueba de integración en `BackendIntegrationTests.java`.
6. El historial de ramas y etiquetas de Git (`v1.0-unidad2`, `v1.2-sc-b`, `v1.3-sc-c`).
