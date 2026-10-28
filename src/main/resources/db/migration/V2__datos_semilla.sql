-- V2__datos_semilla.sql
-- Datos iniciales e idempotentes para demostracion y evaluacion

-- 1. Roles
INSERT INTO rol (id, nombre) VALUES
    (1, 'OPERARIO'),
    (2, 'SUPERVISOR'),
    (3, 'ADMINISTRADOR')
ON CONFLICT (id) DO NOTHING;

-- 2. Privilegios
INSERT INTO privilegio (id, codigo) VALUES
    (1, 'PUNTO_LEER'),
    (2, 'PUNTO_GESTIONAR'),
    (3, 'PESAJE_LEER'),
    (4, 'PESAJE_CREAR'),
    (5, 'PESAJE_EDITAR'),
    (6, 'PESAJE_EDITAR_CUALQUIERA'),
    (7, 'PESAJE_ANULAR'),
    (8, 'RUTA_CERRAR'),
    (9, 'RUTA_PUNTO_OMITIR')
ON CONFLICT (id) DO NOTHING;

-- 3. Asignacion Rol - Privilegios
-- OPERARIO: lectura, registro, correccion propia y omision
INSERT INTO rol_privilegio (rol_id, privilegio_id) VALUES
    (1, 1), (1, 3), (1, 4), (1, 5), (1, 9)
ON CONFLICT DO NOTHING;

-- SUPERVISOR: lectura, registro, correccion total, anulacion, omision y cierre
INSERT INTO rol_privilegio (rol_id, privilegio_id) VALUES
    (2, 1), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9)
ON CONFLICT DO NOTHING;

-- ADMINISTRADOR: lectura y gestion general
INSERT INTO rol_privilegio (rol_id, privilegio_id) VALUES
    (3, 1), (3, 2), (3, 3)
ON CONFLICT DO NOTHING;

-- 4. Usuarios (Password: Eco2026*)
-- Hash: $2a$10$HzamW.zA4LMGC4Mwe4M1euAdhUzDk6MnsN/IyQpOusr9NQgxMALXS
INSERT INTO usuario (id, username, nombre, password_hash, activo) VALUES
    (1, 'operario1', 'Operario Uno', '$2a$10$HzamW.zA4LMGC4Mwe4M1euAdhUzDk6MnsN/IyQpOusr9NQgxMALXS', TRUE),
    (2, 'supervisor1', 'Supervisor Uno', '$2a$10$HzamW.zA4LMGC4Mwe4M1euAdhUzDk6MnsN/IyQpOusr9NQgxMALXS', TRUE),
    (3, 'admin', 'Administrador General', '$2a$10$HzamW.zA4LMGC4Mwe4M1euAdhUzDk6MnsN/IyQpOusr9NQgxMALXS', TRUE),
    (4, 'operario2', 'Operario Dos', '$2a$10$HzamW.zA4LMGC4Mwe4M1euAdhUzDk6MnsN/IyQpOusr9NQgxMALXS', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO usuario_rol (usuario_id, rol_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 1)
ON CONFLICT DO NOTHING;

-- 5. Puntos de Acopio (12 puntos en 3 zonas)
INSERT INTO punto_acopio (id, razon_social, direccion, zona, material_habitual, activo) VALUES
    -- Zona Norte (4 puntos)
    (1, 'Recicladora Norte SAC', 'Av. Tupac Amaru 1234, Comas', 'Norte', 'CARTON', TRUE),
    (2, 'Metales Los Olivos', 'Jr. Paracas 456, Los Olivos', 'Norte', 'METAL', TRUE),
    (3, 'Plasticos San Martin', 'Av. Peru 789, San Martin de Porres', 'Norte', 'PLASTICO', TRUE),
    (4, 'Vidrios Independencia', 'Av. Tupac Amaru 890, Independencia', 'Norte', 'VIDRIO', TRUE),
    -- Zona Centro (4 puntos)
    (5, 'Papelera Central SAC', 'Av. Abancay 555, Cercado de Lima', 'Centro', 'PAPEL', TRUE),
    (6, 'Ecocentro Lima', 'Jr. de la Union 888, Cercado de Lima', 'Centro', 'CARTON', TRUE),
    (7, 'Metales Breña EIRL', 'Av. Arica 1020, Breña', 'Centro', 'METAL', TRUE),
    (8, 'BioRecicla La Victoria', 'Av. Grau 400, La Victoria', 'Centro', 'PLASTICO', TRUE),
    -- Zona Sur (4 puntos)
    (9, 'SurVerde Chorrillos', 'Av. Huaylas 1500, Chorrillos', 'Sur', 'PLASTICO', TRUE),
    (10, 'San Juan Recicla', 'Av. Los Heroes 670, San Juan de Miraflores', 'Sur', 'VIDRIO', TRUE),
    (11, 'Cartonera Miraflores', 'Av. Benavides 2100, Miraflores', 'Sur', 'CARTON', TRUE),
    (12, 'Ecometales Surco', 'Av. Tomas Marsano 3400, Santiago de Surco', 'Sur', 'METAL', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 6. Rutas
-- Ruta 1: Hoy (Abierta, con 6 puntos)
INSERT INTO ruta (id, fecha, estado, version) VALUES
    (1, CURRENT_DATE, 'ABIERTA', 0)
ON CONFLICT (id) DO NOTHING;

-- Ruta 2: Ayer (Cerrada)
INSERT INTO ruta (id, fecha, estado, cerrada_en, cerrada_por, version) VALUES
    (2, CURRENT_DATE - INTERVAL '1 day', 'CERRADA', CURRENT_TIMESTAMP - INTERVAL '1 day', 2, 1)
ON CONFLICT (id) DO NOTHING;

-- Asignar usuarios a rutas
INSERT INTO ruta_usuario (ruta_id, usuario_id) VALUES
    (1, 1),
    (1, 4),
    (2, 1)
ON CONFLICT DO NOTHING;

-- 7. Puntos asignados a Rutas (ruta_punto)
-- Ruta 1 (Hoy): 6 puntos
INSERT INTO ruta_punto (id, ruta_id, punto_id, secuencia) VALUES
    (1, 1, 1, 1),
    (2, 1, 2, 2),
    (3, 1, 3, 3),
    (4, 1, 4, 4),
    (5, 1, 5, 5),
    (6, 1, 6, 6)
ON CONFLICT (id) DO NOTHING;

-- Ruta 2 (Ayer): 4 puntos
INSERT INTO ruta_punto (id, ruta_id, punto_id, secuencia) VALUES
    (7, 2, 7, 1),
    (8, 2, 8, 2),
    (9, 2, 9, 3),
    (10, 2, 10, 4)
ON CONFLICT (id) DO NOTHING;

-- 8. Pesajes (15 pesajes totales, con anulados y autorias distintas)
INSERT INTO pesaje (id, ruta_punto_id, fecha_hora, material, peso_bruto, tara, peso_neto, observacion, operario_id, estado, motivo_anulacion, anulado_por, anulado_en, actualizado_en, version) VALUES
    -- Ruta de hoy (Puntos 1 a 4 con pesajes, Puntos 5 y 6 pendientes)
    (1, 1, CURRENT_TIMESTAMP - INTERVAL '4 hours', 'CARTON', 45.00, 5.00, 40.00, 'Cajas compactadas', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    -- Pesaje 2: Hecho por operario2 en la ruta de hoy (demuestra RN-02: operario1 no puede editarlo!)
    (2, 2, CURRENT_TIMESTAMP - INTERVAL '3 hours', 'METAL', 120.00, 10.00, 110.00, 'Chatarra limpia', 4, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    (3, 3, CURRENT_TIMESTAMP - INTERVAL '2 hours', 'PLASTICO', 30.50, 2.50, 28.00, 'Botellas PET prensadas', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    (4, 4, CURRENT_TIMESTAMP - INTERVAL '1 hour', 'VIDRIO', 85.00, 8.00, 77.00, 'Botellas transparentes', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),

    -- Ruta de ayer (Ruta 2 cerrada)
    (5, 7, CURRENT_TIMESTAMP - INTERVAL '26 hours', 'METAL', 55.00, 5.00, 50.00, 'Cobre seleccionado', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    -- Pesaje 6: Anulado logicamente por supervisor (demuestra regla de anulacion)
    (6, 8, CURRENT_TIMESTAMP - INTERVAL '25 hours', 'PLASTICO', 40.00, 15.00, 25.00, 'Bolsas plasticas', 1, 'ANULADO', 'Tara incorrecta reportada por operario', 2, CURRENT_TIMESTAMP - INTERVAL '24 hours', CURRENT_TIMESTAMP, 1),
    -- Pesaje 7: Reemplazo corregido del punto 8
    (7, 8, CURRENT_TIMESTAMP - INTERVAL '24 hours', 'PLASTICO', 40.00, 5.00, 35.00, 'Bolsas plasticas con tara corregida', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    (8, 9, CURRENT_TIMESTAMP - INTERVAL '23 hours', 'VIDRIO', 90.00, 10.00, 80.00, 'Frascos de conserva', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    (9, 10, CURRENT_TIMESTAMP - INTERVAL '22 hours', 'PAPEL', 65.00, 5.00, 60.00, 'Archivo muerto', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    (10, 7, CURRENT_TIMESTAMP - INTERVAL '21 hours', 'CARTON', 35.00, 3.00, 32.00, 'Carton corrugado', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    (11, 7, CURRENT_TIMESTAMP - INTERVAL '20 hours', 'METAL', 45.00, 4.00, 41.00, 'Aluminio en latas', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    (12, 9, CURRENT_TIMESTAMP - INTERVAL '19 hours', 'PLASTICO', 25.00, 2.00, 23.00, 'Tapas plasticas', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    (13, 9, CURRENT_TIMESTAMP - INTERVAL '18 hours', 'CARTON', 80.00, 8.00, 72.00, 'Cajas plegadas', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    (14, 10, CURRENT_TIMESTAMP - INTERVAL '17 hours', 'VIDRIO', 110.00, 12.00, 98.00, 'Vidrio verde', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0),
    (15, 10, CURRENT_TIMESTAMP - INTERVAL '16 hours', 'PAPEL', 50.00, 5.00, 45.00, 'Periodicos y revistas', 1, 'REGISTRADO', NULL, NULL, NULL, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Sincronizar secuencias de PostgreSQL
SELECT setval('usuario_id_seq', (SELECT MAX(id) FROM usuario));
SELECT setval('rol_id_seq', (SELECT MAX(id) FROM rol));
SELECT setval('privilegio_id_seq', (SELECT MAX(id) FROM privilegio));
SELECT setval('punto_acopio_id_seq', (SELECT MAX(id) FROM punto_acopio));
SELECT setval('ruta_id_seq', (SELECT MAX(id) FROM ruta));
SELECT setval('ruta_punto_id_seq', (SELECT MAX(id) FROM ruta_punto));
SELECT setval('pesaje_id_seq', (SELECT MAX(id) FROM pesaje));

