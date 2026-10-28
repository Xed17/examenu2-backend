-- V1__esquema_inicial.sql
-- Tablas de seguridad y control de acceso

CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS rol (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS privilegio (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS usuario_rol (
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    rol_id BIGINT NOT NULL REFERENCES rol(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
);

CREATE TABLE IF NOT EXISTS rol_privilegio (
    rol_id BIGINT NOT NULL REFERENCES rol(id) ON DELETE CASCADE,
    privilegio_id BIGINT NOT NULL REFERENCES privilegio(id) ON DELETE CASCADE,
    PRIMARY KEY (rol_id, privilegio_id)
);

-- Tablas del negocio

CREATE TABLE IF NOT EXISTS punto_acopio (
    id BIGSERIAL PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    zona VARCHAR(50) NOT NULL,
    material_habitual VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS ruta (
    id BIGSERIAL PRIMARY KEY,
    fecha DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    cerrada_en TIMESTAMPTZ,
    cerrada_por BIGINT REFERENCES usuario(id),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS ruta_usuario (
    ruta_id BIGINT NOT NULL REFERENCES ruta(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    PRIMARY KEY (ruta_id, usuario_id)
);

CREATE TABLE IF NOT EXISTS ruta_punto (
    id BIGSERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL REFERENCES ruta(id) ON DELETE CASCADE,
    punto_id BIGINT NOT NULL REFERENCES punto_acopio(id),
    secuencia INT NOT NULL,
    motivo_omision VARCHAR(255),
    omitido_por BIGINT REFERENCES usuario(id),
    omitido_en TIMESTAMPTZ,
    CONSTRAINT uq_ruta_punto_secuencia UNIQUE (ruta_id, secuencia),
    CONSTRAINT uq_ruta_punto_punto UNIQUE (ruta_id, punto_id)
);

CREATE TABLE IF NOT EXISTS pesaje (
    id BIGSERIAL PRIMARY KEY,
    ruta_punto_id BIGINT NOT NULL REFERENCES ruta_punto(id),
    fecha_hora TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    material VARCHAR(50) NOT NULL,
    peso_bruto NUMERIC(10, 2) NOT NULL CHECK (peso_bruto > 0),
    tara NUMERIC(10, 2) NOT NULL CHECK (tara >= 0),
    peso_neto NUMERIC(10, 2) NOT NULL CHECK (peso_neto > 0),
    observacion VARCHAR(255),
    operario_id BIGINT NOT NULL REFERENCES usuario(id),
    estado VARCHAR(20) NOT NULL DEFAULT 'REGISTRADO',
    motivo_anulacion VARCHAR(255),
    anulado_por BIGINT REFERENCES usuario(id),
    anulado_en TIMESTAMPTZ,
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    operacion_id VARCHAR(64) UNIQUE
);

CREATE TABLE IF NOT EXISTS operacion_cliente (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    operacion_uuid VARCHAR(64) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    recurso_id BIGINT,
    creada_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usuario_operacion UNIQUE (usuario_id, operacion_uuid)
);

-- Indices
CREATE INDEX IF NOT EXISTS idx_pesaje_ruta_punto ON pesaje(ruta_punto_id);
CREATE INDEX IF NOT EXISTS idx_pesaje_operario ON pesaje(operario_id);
CREATE INDEX IF NOT EXISTS idx_ruta_fecha ON ruta(fecha);
CREATE INDEX IF NOT EXISTS idx_ruta_punto_ruta ON ruta_punto(ruta_id);

