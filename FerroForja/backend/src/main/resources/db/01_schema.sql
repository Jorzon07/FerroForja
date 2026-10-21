-- =============================================================================
-- PROYECTO: Sistema Web de Gestión de Inventario para Ferretería
-- ETAPA 4: Script DDL de Creación del Esquema de Base de Datos MySQL 8.0
-- ARCHIVO: 01_schema.sql
-- DESCRIPCIÓN: Estructura de tablas, claves primarias, claves foráneas,
--              restricciones UNIQUE, restricciones CHECK e índices.
-- =============================================================================

SET NAMES 'utf8mb4';
SET CHARACTER SET utf8mb4;
SET character_set_connection = 'utf8mb4';
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- 1. IDENTIDAD Y SEGURIDAD
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS bitacora_log;
DROP TABLE IF EXISTS restitucion_detalle;
DROP TABLE IF EXISTS restitucion;
DROP TABLE IF EXISTS expendio_detalle;
DROP TABLE IF EXISTS expendio;
DROP TABLE IF EXISTS abastecimiento_detalle;
DROP TABLE IF EXISTS abastecimiento;
DROP TABLE IF EXISTS reajuste_almacen;
DROP TABLE IF EXISTS almacen_asiento;
DROP TABLE IF EXISTS almacen_nivel;
DROP TABLE IF EXISTS ficha;
DROP TABLE IF EXISTS empaque;
DROP TABLE IF EXISTS fabricante;
DROP TABLE IF EXISTS articulo;
DROP TABLE IF EXISTS medida_base;
DROP TABLE IF EXISTS socio;
DROP TABLE IF EXISTS tiquete_correlativo;
DROP TABLE IF EXISTS operario_perfil;
DROP TABLE IF EXISTS perfil;
DROP TABLE IF EXISTS operario;

SET FOREIGN_KEY_CHECKS = 1;

-- Tabla de Usuarios
CREATE TABLE operario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nombre_visible VARCHAR(120) NOT NULL,
    email VARCHAR(100) NULL,
    habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    actualizado_en DATETIME(6) NULL ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_operario_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Roles
CREATE TABLE perfil (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    denominacion VARCHAR(40) NOT NULL,
    resena VARCHAR(150) NULL,
    CONSTRAINT uk_perfil_denominacion UNIQUE (denominacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla Asociativa Usuario - Rol
CREATE TABLE operario_perfil (
    operario_id BIGINT NOT NULL,
    perfil_id BIGINT NOT NULL,
    PRIMARY KEY (operario_id, perfil_id),
    CONSTRAINT fk_usuariorol_operario FOREIGN KEY (operario_id) REFERENCES operario(id) ON DELETE CASCADE,
    CONSTRAINT fk_usuariorol_perfil FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. TERCEROS (CLIENTES Y PROVEEDORES UNIFICADOS - RN-10)
-- -----------------------------------------------------------------------------
CREATE TABLE socio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_identificacion VARCHAR(10) NOT NULL,
    nro_identificacion VARCHAR(30) NOT NULL,
    nombre_legal VARCHAR(150) NOT NULL,
    tipo_persona VARCHAR(15) NOT NULL,
    celular VARCHAR(30) NULL,
    email VARCHAR(100) NULL,
    ubicacion VARCHAR(150) NULL,
    es_comprador BOOLEAN NOT NULL DEFAULT TRUE,
    es_suministrador BOOLEAN NOT NULL DEFAULT FALSE,
    habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_socio_folio UNIQUE (nro_identificacion),
    CONSTRAINT chk_socio_tipo_doc CHECK (tipo_identificacion IN ('CC','NIT','CE','PASAPORTE')),
    CONSTRAINT chk_socio_tipo_persona CHECK (tipo_persona IN ('NATURAL','EMPRESA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_socio_folio ON socio(nro_identificacion);
CREATE INDEX idx_socio_nombre_legal ON socio(nombre_legal);

-- -----------------------------------------------------------------------------
-- 3. CATÁLOGO DE PRODUCTOS Y REFERENCIAS COMERCIALES
-- -----------------------------------------------------------------------------
CREATE TABLE medida_base (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(10) NOT NULL,
    denominacion VARCHAR(50) NOT NULL,
    CONSTRAINT uk_medida_clave UNIQUE (clave)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE articulo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    denominacion VARCHAR(100) NOT NULL,
    resena VARCHAR(255) NULL,
    habilitado BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE fabricante (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    denominacion VARCHAR(80) NOT NULL,
    habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_fabricante_denominacion UNIQUE (denominacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE empaque (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    denominacion VARCHAR(80) NOT NULL,
    habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_empaque_denominacion UNIQUE (denominacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ficha (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    articulo_id BIGINT NOT NULL,
    fabricante_id BIGINT NOT NULL,
    empaque_id BIGINT NOT NULL,
    medida_base_id BIGINT NOT NULL,
    codigo_ean VARCHAR(50) NOT NULL,
    tarifa_publico DECIMAL(15, 2) NOT NULL,
    habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_fch_codigo_ean UNIQUE (codigo_ean),
    CONSTRAINT uk_fch_prod_fabricante_emp UNIQUE (articulo_id, fabricante_id, empaque_id),
    CONSTRAINT chk_fch_tarifa CHECK (tarifa_publico >= 0),
    CONSTRAINT fk_fch_articulo FOREIGN KEY (articulo_id) REFERENCES articulo(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fch_fabricante FOREIGN KEY (fabricante_id) REFERENCES fabricante(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fch_empaque FOREIGN KEY (empaque_id) REFERENCES empaque(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fch_medida FOREIGN KEY (medida_base_id) REFERENCES medida_base(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_fch_codigo_ean ON ficha(codigo_ean);

-- -----------------------------------------------------------------------------
-- 4. INVENTARIO, SALDOS Y MOVIMIENTOS (KÁRDEX)
-- -----------------------------------------------------------------------------
CREATE TABLE almacen_nivel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ficha_id BIGINT NOT NULL,
    volumen_libre DECIMAL(12, 3) NOT NULL DEFAULT 0.000,
    coste_medio_ponderado DECIMAL(15, 4) NOT NULL DEFAULT 0.0000,
    actualizado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_nivel_ficha UNIQUE (ficha_id),
    CONSTRAINT chk_nivel_volumen CHECK (volumen_libre >= 0),
    CONSTRAINT chk_nivel_cpp CHECK (coste_medio_ponderado >= 0),
    CONSTRAINT fk_nivel_ficha FOREIGN KEY (ficha_id) REFERENCES ficha(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE almacen_asiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ficha_id BIGINT NOT NULL,
    tipo_asiento VARCHAR(30) NOT NULL,
    volumen DECIMAL(12, 3) NOT NULL,
    coste_individual_asiento DECIMAL(15, 4) NOT NULL,
    nivel_obtenido DECIMAL(12, 3) NOT NULL,
    folio_tipo VARCHAR(20) NOT NULL,
    folio_id BIGINT NOT NULL,
    causa VARCHAR(255) NULL,
    marca_tiempo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    operario_id BIGINT NOT NULL,
    CONSTRAINT chk_asi_volumen CHECK (volumen > 0),
    CONSTRAINT chk_asi_coste CHECK (coste_individual_asiento >= 0),
    CONSTRAINT chk_asi_nivel CHECK (nivel_obtenido >= 0),
    CONSTRAINT fk_asi_ficha FOREIGN KEY (ficha_id) REFERENCES ficha(id) ON DELETE RESTRICT,
    CONSTRAINT fk_asi_operario FOREIGN KEY (operario_id) REFERENCES operario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_asi_fch_fecha ON almacen_asiento(ficha_id, marca_tiempo);
CREATE INDEX idx_asi_doc ON almacen_asiento(folio_tipo, folio_id);

-- -----------------------------------------------------------------------------
-- 5. COMPRAS Y LÍNEAS DE COMPRA
-- -----------------------------------------------------------------------------
CREATE TABLE abastecimiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    correlativo VARCHAR(25) NOT NULL,
    suministrador_id BIGINT NOT NULL,
    marca_tiempo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    total DECIMAL(15, 2) NOT NULL,
    situacion VARCHAR(20) NOT NULL DEFAULT 'RECIBIDA',
    causa_anulacion VARCHAR(255) NULL,
    operario_id BIGINT NOT NULL,
    CONSTRAINT uk_abastecimiento_correlativo UNIQUE (correlativo),
    CONSTRAINT chk_abastecimiento_total CHECK (total >= 0),
    CONSTRAINT chk_abastecimiento_situacion CHECK (situacion IN ('RECIBIDA','ANULADA')),
    CONSTRAINT fk_abastecimiento_suministrador FOREIGN KEY (suministrador_id) REFERENCES socio(id) ON DELETE RESTRICT,
    CONSTRAINT fk_abastecimiento_operario FOREIGN KEY (operario_id) REFERENCES operario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE abastecimiento_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    abastecimiento_id BIGINT NOT NULL,
    ficha_id BIGINT NOT NULL,
    volumen DECIMAL(12, 3) NOT NULL,
    coste_individual DECIMAL(15, 4) NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL,
    CONSTRAINT chk_abastdetalle_vol CHECK (volumen > 0),
    CONSTRAINT chk_abastdetalle_coste CHECK (coste_individual >= 0),
    CONSTRAINT chk_abastdetalle_subtotal CHECK (subtotal >= 0),
    CONSTRAINT fk_abastdetalle_abastecimiento FOREIGN KEY (abastecimiento_id) REFERENCES abastecimiento(id) ON DELETE CASCADE,
    CONSTRAINT fk_abastdetalle_fch FOREIGN KEY (ficha_id) REFERENCES ficha(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. VENTAS DE CONTADO Y LÍNEAS DE VENTA
-- -----------------------------------------------------------------------------
CREATE TABLE expendio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    correlativo VARCHAR(25) NOT NULL,
    comprador_id BIGINT NOT NULL,
    marca_tiempo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    subtotal DECIMAL(15, 2) NOT NULL,
    total DECIMAL(15, 2) NOT NULL,
    importe_abonado_efectivo DECIMAL(15, 2) NOT NULL,
    vuelto_entregado DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    situacion VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA',
    causa_anulacion VARCHAR(255) NULL,
    operario_id BIGINT NOT NULL,
    CONSTRAINT uk_expendio_correlativo UNIQUE (correlativo),
    CONSTRAINT chk_expendio_total CHECK (total >= 0),
    CONSTRAINT chk_expendio_pago CHECK (importe_abonado_efectivo >= total),
    CONSTRAINT chk_expendio_situacion CHECK (situacion IN ('COMPLETADA','ANULADA')),
    CONSTRAINT fk_expendio_comprador FOREIGN KEY (comprador_id) REFERENCES socio(id) ON DELETE RESTRICT,
    CONSTRAINT fk_expendio_operario FOREIGN KEY (operario_id) REFERENCES operario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE expendio_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expendio_id BIGINT NOT NULL,
    ficha_id BIGINT NOT NULL,
    volumen DECIMAL(12, 3) NOT NULL,
    tarifa_individual DECIMAL(15, 2) NOT NULL,
    coste_congelado_individual DECIMAL(15, 4) NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL,
    CONSTRAINT chk_expendiodetalle_vol CHECK (volumen > 0),
    CONSTRAINT chk_expendiodetalle_tarifa CHECK (tarifa_individual >= 0),
    CONSTRAINT chk_expendiodetalle_coste CHECK (coste_congelado_individual >= 0),
    CONSTRAINT chk_expendiodetalle_subtotal CHECK (subtotal >= 0),
    CONSTRAINT fk_expendiodetalle_expendio FOREIGN KEY (expendio_id) REFERENCES expendio(id) ON DELETE CASCADE,
    CONSTRAINT fk_expendiodetalle_fch FOREIGN KEY (ficha_id) REFERENCES ficha(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. DEVOLUCIONES DE CLIENTES
-- -----------------------------------------------------------------------------
CREATE TABLE restitucion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    correlativo VARCHAR(25) NOT NULL,
    expendio_id BIGINT NULL,
    comprador_id BIGINT NULL,
    tipo_restitucion VARCHAR(20) NOT NULL,
    marca_tiempo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    causa VARCHAR(255) NOT NULL,
    situacion VARCHAR(20) NOT NULL DEFAULT 'APROBADA',
    operario_id BIGINT NOT NULL,
    CONSTRAINT uk_dev_correlativo UNIQUE (correlativo),
    CONSTRAINT chk_dev_tipo CHECK (tipo_restitucion IN ('VINCULADA','NO_VINCULADA')),
    CONSTRAINT fk_dev_expendio FOREIGN KEY (expendio_id) REFERENCES expendio(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dev_comprador FOREIGN KEY (comprador_id) REFERENCES socio(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dev_operario FOREIGN KEY (operario_id) REFERENCES operario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE restitucion_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    restitucion_id BIGINT NOT NULL,
    ficha_id BIGINT NOT NULL,
    volumen DECIMAL(12, 3) NOT NULL,
    coste_individual_aplicado DECIMAL(15, 4) NOT NULL,
    CONSTRAINT chk_restdet_vol CHECK (volumen > 0),
    CONSTRAINT chk_restdet_coste CHECK (coste_individual_aplicado >= 0),
    CONSTRAINT fk_restdet_dev FOREIGN KEY (restitucion_id) REFERENCES restitucion(id) ON DELETE CASCADE,
    CONSTRAINT fk_restdet_fch FOREIGN KEY (ficha_id) REFERENCES ficha(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. AJUSTES DE INVENTARIO (ADMINISTRADOR EXCLUSIVO - RN-08)
-- -----------------------------------------------------------------------------
CREATE TABLE reajuste_almacen (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    correlativo VARCHAR(25) NOT NULL,
    ficha_id BIGINT NOT NULL,
    tipo_reajuste VARCHAR(20) NOT NULL,
    volumen DECIMAL(12, 3) NOT NULL,
    coste_individual_ficha DECIMAL(15, 4) NOT NULL,
    causa VARCHAR(255) NOT NULL,
    marca_tiempo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    operario_id BIGINT NOT NULL,
    CONSTRAINT uk_reajuste_correlativo UNIQUE (correlativo),
    CONSTRAINT chk_reajuste_tipo CHECK (tipo_reajuste IN ('INCREMENTO','DISMINUCION')),
    CONSTRAINT chk_reajuste_vol CHECK (volumen > 0),
    CONSTRAINT fk_reajuste_fch FOREIGN KEY (ficha_id) REFERENCES ficha(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reajuste_operario FOREIGN KEY (operario_id) REFERENCES operario(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. CONTROL DE CONSECUTIVOS Y AUDITORÍA BÁSICA (RN-11, RN-12)
-- -----------------------------------------------------------------------------
CREATE TABLE tiquete_correlativo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_identificacion VARCHAR(25) NOT NULL,
    prefijo VARCHAR(10) NOT NULL,
    ultimo_nro BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_correlativo_tipo UNIQUE (tipo_identificacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bitacora_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operario_id BIGINT NULL,
    marca_tiempo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    modulo VARCHAR(40) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    resena TEXT NOT NULL,
    CONSTRAINT fk_audit_operario FOREIGN KEY (operario_id) REFERENCES operario(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bitacora_fecha ON bitacora_log(marca_tiempo);
