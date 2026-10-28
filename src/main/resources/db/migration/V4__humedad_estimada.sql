-- V4__humedad_estimada.sql
-- Solicitud de Cambio SC-C: Captura de porcentaje de humedad estimada en el pesaje

ALTER TABLE pesaje ADD COLUMN humedad_estimada NUMERIC(5,2) NULL;

ALTER TABLE pesaje ADD CONSTRAINT chk_pesaje_humedad_estimada
    CHECK (humedad_estimada IS NULL OR (humedad_estimada >= 0 AND humedad_estimada <= 100));

