-- V3__rol_auditor.sql
-- Solicitud de Cambio SC-B: Rol AUDITOR con permisos de solo lectura

-- 1. Insertar Rol AUDITOR
INSERT INTO rol (id, nombre) VALUES (4, 'AUDITOR')
ON CONFLICT (id) DO NOTHING;

-- 2. Asignar privilegios de solo lectura al rol AUDITOR
-- 1: PUNTO_LEER, 3: PESAJE_LEER
INSERT INTO rol_privilegio (rol_id, privilegio_id) VALUES
    (4, 1),
    (4, 3)
ON CONFLICT DO NOTHING;

-- 3. Crear usuario auditor1
INSERT INTO usuario (id, username, nombre, password_hash, activo) VALUES
    (5, 'auditor1', 'Auditor de Calidad', '$2a$10$HzamW.zA4LMGC4Mwe4M1euAdhUzDk6MnsN/IyQpOusr9NQgxMALXS', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 4. Asignar rol AUDITOR a auditor1
INSERT INTO usuario_rol (usuario_id, rol_id) VALUES
    (5, 4)
ON CONFLICT DO NOTHING;

-- 5. Asignar auditor1 a la Ruta 1 (de hoy) para que pueda consultar sus puntos y pesajes
INSERT INTO ruta_usuario (ruta_id, usuario_id) VALUES
    (1, 5)
ON CONFLICT DO NOTHING;

