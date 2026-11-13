-- Apply once to an existing Little Horizons schema.
-- Reconciles yearly counters with persisted documents and prevents first invoice
-- from starting at 00002 after the original seed initialized the counter at 1.
USE erp_little_horizons;

INSERT INTO secuencias_financieras (nombre_secuencia, año, ultimo_valor)
SELECT 'recibo', CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(numero_recibo, '-', 2), '-', -1) AS UNSIGNED),
       MAX(CAST(SUBSTRING_INDEX(numero_recibo, '-', -1) AS UNSIGNED))
FROM pagos
WHERE numero_recibo REGEXP '^REC-[0-9]{4}-[0-9]+$'
GROUP BY CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(numero_recibo, '-', 2), '-', -1) AS UNSIGNED)
ON DUPLICATE KEY UPDATE ultimo_valor = GREATEST(ultimo_valor, VALUES(ultimo_valor));

-- Correct the original erroneous seed (1) when that year has no persisted invoice.
UPDATE secuencias_financieras s
LEFT JOIN (
    SELECT CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(numero_factura, '-', 2), '-', -1) AS UNSIGNED) AS año
    FROM facturas
    WHERE numero_factura REGEXP '^FAC-[0-9]{4}-[0-9]+$'
    GROUP BY CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(numero_factura, '-', 2), '-', -1) AS UNSIGNED)
) f ON f.año = s.año
SET s.ultimo_valor = 0
WHERE s.nombre_secuencia = 'factura' AND s.ultimo_valor = 1 AND f.año IS NULL;

INSERT INTO secuencias_financieras (nombre_secuencia, año, ultimo_valor)
SELECT 'factura', CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(numero_factura, '-', 2), '-', -1) AS UNSIGNED),
       MAX(CAST(SUBSTRING_INDEX(numero_factura, '-', -1) AS UNSIGNED))
FROM facturas
WHERE numero_factura REGEXP '^FAC-[0-9]{4}-[0-9]+$'
GROUP BY CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(numero_factura, '-', 2), '-', -1) AS UNSIGNED)
ON DUPLICATE KEY UPDATE ultimo_valor = GREATEST(ultimo_valor, VALUES(ultimo_valor));

-- The index is already present in docs/database.sql for fresh installations.
-- For an older schema, check INFORMATION_SCHEMA.STATISTICS before applying this DDL.
-- ALTER TABLE registros_asistencia
--   ADD INDEX idx_estado_fecha_estudiante (estado_asistencia, fecha_asistencia, id_estudiante);
