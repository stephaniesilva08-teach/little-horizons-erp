-- Apply once to an existing Little Horizons schema, with the application offline.
-- Backfills historical payments to matching student/concept/year/period charges
-- using FIFO order and retains any excess payment as an unapplied credit.
USE erp_little_horizons;

ALTER TABLE pagos
    ADD COLUMN monto_no_aplicado DECIMAL(10,2) NOT NULL DEFAULT 0
        COMMENT 'Crédito del pago que aún no está aplicado a un cargo'
        AFTER monto_pagado,
    ADD CONSTRAINT chk_pago_credito_no_aplicado
        CHECK (monto_no_aplicado >= 0 AND monto_no_aplicado <= monto_pagado),
    ADD INDEX idx_pago_creditos (id_estudiante, id_concepto, año_academico,
                                 periodo_academico, fecha_pago, id_pago);

CREATE TABLE pagos_cargos (
    id_aplicacion INT AUTO_INCREMENT PRIMARY KEY,
    id_pago INT NOT NULL,
    id_cargo INT NOT NULL,
    monto_aplicado DECIMAL(10,2) NOT NULL,
    fecha_aplicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_pago) REFERENCES pagos(id_pago) ON DELETE RESTRICT,
    FOREIGN KEY (id_cargo) REFERENCES cargos_estudiante(id_cargo) ON DELETE RESTRICT,
    CHECK (monto_aplicado > 0),
    UNIQUE KEY aplicacion_pago_cargo_unica (id_pago, id_cargo),
    INDEX idx_aplicacion_cargo (id_cargo, id_pago)
) ENGINE=InnoDB COMMENT='Trazabilidad del valor de cada pago aplicado a obligaciones';

INSERT INTO pagos_cargos (id_pago, id_cargo, monto_aplicado)
SELECT p.id_pago, c.id_cargo,
       LEAST(p.pago_fin, c.cargo_fin) - GREATEST(p.pago_inicio, c.cargo_inicio)
FROM (
    SELECT id_pago, id_estudiante, id_concepto, año_academico, periodo_academico,
           SUM(monto_pagado) OVER payment_order AS pago_fin,
           SUM(monto_pagado) OVER payment_order - monto_pagado AS pago_inicio
    FROM pagos
    WINDOW payment_order AS (
        PARTITION BY id_estudiante, id_concepto, año_academico, periodo_academico
        ORDER BY fecha_pago, id_pago
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    )
) p
INNER JOIN (
    SELECT id_cargo, id_estudiante, id_concepto, año_academico, periodo_academico,
           SUM(monto) OVER charge_order AS cargo_fin,
           SUM(monto) OVER charge_order - monto AS cargo_inicio
    FROM cargos_estudiante
    WHERE estado <> 'Anulado'
    WINDOW charge_order AS (
        PARTITION BY id_estudiante, id_concepto, año_academico, periodo_academico
        ORDER BY fecha_vencimiento, id_cargo
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    )
) c ON c.id_estudiante = p.id_estudiante
    AND c.id_concepto = p.id_concepto
    AND c.año_academico = p.año_academico
    AND c.periodo_academico <=> p.periodo_academico
WHERE LEAST(p.pago_fin, c.cargo_fin) > GREATEST(p.pago_inicio, c.cargo_inicio);

UPDATE pagos p
LEFT JOIN (
    SELECT id_pago, SUM(monto_aplicado) AS total_aplicado
    FROM pagos_cargos
    GROUP BY id_pago
) a ON a.id_pago = p.id_pago
SET p.monto_no_aplicado = GREATEST(p.monto_pagado - COALESCE(a.total_aplicado, 0), 0);

UPDATE cargos_estudiante c
LEFT JOIN (
    SELECT id_cargo, SUM(monto_aplicado) AS total_aplicado
    FROM pagos_cargos
    GROUP BY id_cargo
) a ON a.id_cargo = c.id_cargo
SET c.estado = CASE
    WHEN COALESCE(a.total_aplicado, 0) >= c.monto THEN 'Pagado'
    ELSE 'Pendiente'
END
WHERE c.estado <> 'Anulado';
