-- =============================================================================
-- PROYECTO: Sistema Web de Gestión de Inventario para Ferretería (FerroForja)
-- ETAPA 4: Script DML de Datos Iniciales y Demostración Sintética
-- ARCHIVO: 02_initial_data.sql
-- =============================================================================

SET NAMES 'utf8mb4';
SET CHARACTER SET utf8mb4;
SET character_set_connection = 'utf8mb4';
SET FOREIGN_KEY_CHECKS = 0;

-- 1. ROLES DEL SISTEMA
INSERT INTO perfil (id, denominacion, resena) VALUES
(1, 'ROLE_ADMINISTRADOR', 'Administrador con acceso y control total del sistema'),
(2, 'ROLE_USUARIO_OPERATIVO', 'Usuario de mostrador para ventas, compras y consultas')
ON DUPLICATE KEY UPDATE denominacion=VALUES(denominacion), resena=VALUES(resena);

-- 2. USUARIOS INICIALES (Contraseñas cifradas con hash BCrypt coste 10)
-- Credenciales:
--   gerente_forja:      Forja#2026Master!
--   jefe_almacen:       Bodega#2026Segura!
--   cajero_mostrador:   Caja#2026Mostrador!
--   admin_forja:        Forja#Admin26
INSERT INTO operario (id, username, password_hash, nombre_visible, email, habilitado) VALUES
(1, 'gerente_forja', '$2a$10$FqsVw51nGh.oeiAfKs4vz.eKDNh9A6444mmbNzgnW7ukLeelpXrsK', 'Ing. Germán Ortiz - Gerente General', 'gerencia@ferroforja.com.co', TRUE),
(2, 'jefe_almacen', '$2a$10$CrXPfKR8Ul3/cCKUCO7yOuZ5YghC70GFDCAPcSueIDSlUpeBbaWHK', 'Andrés Vega - Jefe de Almacén y Control de Stock', 'almacen@ferroforja.com.co', TRUE),
(3, 'cajero_mostrador', '$2a$10$8f1wFGurFJyPEB3BWPY/T.KUnZtd4ND/oPli.aOnUEpMTc3HkN6YK', 'Daniela Morales - Cajera Principal Mostrador', 'caja@ferroforja.com.co', TRUE),
(4, 'admin_forja', '$2a$10$JHyEOf.Qu4Nob3QpCQzLh.bjow9B/VWOwsvSTHjLL08hjpJdnnONy', 'Administrador Técnico de Sistema', 'admin@ferroforja.local', TRUE)
ON DUPLICATE KEY UPDATE password_hash=VALUES(password_hash), nombre_visible=VALUES(nombre_visible), email=VALUES(email), habilitado=VALUES(habilitado);

INSERT INTO operario_perfil (operario_id, perfil_id) VALUES
(1, 1), -- gerente_forja -> ROLE_ADMINISTRADOR
(2, 1), -- jefe_almacen -> ROLE_ADMINISTRADOR
(3, 2), -- cajero_mostrador -> ROLE_USUARIO_OPERATIVO
(4, 1)  -- admin_forja -> ROLE_ADMINISTRADOR
ON DUPLICATE KEY UPDATE perfil_id=VALUES(perfil_id);

-- 3. UNIDADES DE MEDIDA ESTÁNDAR
INSERT INTO medida_base (id, clave, denominacion) VALUES
(1, 'UND', 'Unidad'),
(2, 'MTR', 'Metro lineal'),
(3, 'KGM', 'Kilogramo'),
(4, 'GLN', 'Galón'),
(5, 'CJA', 'Caja'),
(6, 'BLT', 'Bulto')
ON DUPLICATE KEY UPDATE clave=VALUES(clave), denominacion=VALUES(denominacion);

-- 4. MARCAS COMERCIALES RECONOCIDAS
INSERT INTO fabricante (id, denominacion, habilitado) VALUES
(1, 'Herragro', TRUE),
(2, 'Stanley', TRUE),
(3, 'Pintuco', TRUE),
(4, 'Pavco Wavin', TRUE),
(5, 'Bellota', TRUE),
(6, 'Sika Colombia', TRUE),
(7, 'Bosch', TRUE),
(8, 'Gyplac', TRUE),
(9, 'Gerfor', TRUE),
(10, 'Acesco', TRUE)
ON DUPLICATE KEY UPDATE denominacion=VALUES(denominacion);

-- 5. PRESENTACIONES COMERCIALES
INSERT INTO empaque (id, denominacion, habilitado) VALUES
(1, 'Unidad Suelta', TRUE),
(2, 'Caja x 100', TRUE),
(3, 'Caja x 500', TRUE),
(4, 'Galón 3.78L', TRUE),
(5, 'Cuñete 5 Galones', TRUE),
(6, 'Tubo 6 Metros', TRUE),
(7, 'Rollo 50m', TRUE),
(8, 'Bulto 50 Kg', TRUE),
(9, 'Bolsa 1 Kg', TRUE),
(10, 'Kit / Blíster', TRUE)
ON DUPLICATE KEY UPDATE denominacion=VALUES(denominacion);

-- 6. PRODUCTOS BASE (ARTÍCULOS)
INSERT INTO articulo (id, denominacion, resena, habilitado) VALUES
(1, 'Martillo de Uña 16oz Forjado', 'Martillo forjado de acero con mango de fibra de vidrio ergonómico', TRUE),
(2, 'Tornillo Drywall 1 1/4 pulg Fosfatado', 'Tornillo de fijación para láminas de yeso y fibrocemento punta broca', TRUE),
(3, 'Pintura Vinilo Tipo 1 Blanco Pro', 'Pintura base agua de alta lavabilidad para interiores y fachadas', TRUE),
(4, 'Tubo PVC Presión RDE 9 1/2 pulg x 6m', 'Tubo para conducción de agua potable a presión norma NTC 382', TRUE),
(5, 'Alicate Universal Electricista 8 pulg Aislado', 'Alicate forjado con aislamiento 1000V para electricista', TRUE),
(6, 'Taladro Percutor 1/2 pulg 650W GSB 13 RE', 'Taladro percutor industrial con velocidad variable y reversible', TRUE),
(7, 'Impermeabilizante Sika 1 Líquido Frasco 4 Kg', 'Aditivo hidrófugo de fraguado normal para morteros y pañetes', TRUE),
(8, 'Pala Redonda Herrada No. 4 Cabo Madera', 'Pala para mezcla, excavación y carga en obra civil', TRUE),
(9, 'Disco de Corte Metal 4 1/2 pulg x 1 mm Standard', 'Disco abrasivo fino para corte rápido de acero y perfiles', TRUE),
(10, 'Cinta Métrica Global Plus 5m / 16ft', 'Flexómetro de alta visibilidad con cinta recubierta en polímero', TRUE),
(11, 'Cemento Gris Uso General Tipo UG Bulto 50kg', 'Cemento estructural para fundición de columnas, placas y vigas', TRUE),
(12, 'Llave de Tubo Stilson 12 pulg Heavy Duty', 'Llave para tubería y fontanería pesada con mandíbula forjada', TRUE),
(13, 'Anticorrosivo Alquídico Rojo Óxido 1 Galón', 'Pintura base anticorrosiva para metales ferrosos expuestos', TRUE),
(14, 'Soldadura Líquida PVC Presión 1/4 Galón', 'Pegante de viscosidad media para unión hermética de tubos PVC', TRUE),
(15, 'Guante de Vaqueta Tipo Ingeniero Reforzado', 'Guante de cuero vacuno para protección manual en trabajo pesado', TRUE)
ON DUPLICATE KEY UPDATE denominacion=VALUES(denominacion), resena=VALUES(resena);

-- 7. REFERENCIAS COMERCIALES VENDIBLES (FICHAS)
INSERT INTO ficha (id, articulo_id, fabricante_id, empaque_id, medida_base_id, codigo_ean, tarifa_publico, habilitado) VALUES
(1, 1, 1, 1, 1, '7701234567891', 38500.00, TRUE), -- Martillo Herragro Unidad
(2, 2, 8, 2, 5, '7701234567892', 16900.00, TRUE), -- Tornillo Drywall Gyplac Caja x 100
(3, 3, 3, 4, 4, '7701234567893', 84900.00, TRUE), -- Pintura Vinilo Pintuco Galón
(4, 4, 4, 6, 2, '7701234567894', 21500.00, TRUE), -- Tubo PVC Pavco Wavin Tubo 6m
(5, 5, 2, 1, 1, '7701234567895', 46000.00, TRUE), -- Alicate Universal Stanley Unidad
(6, 6, 7, 1, 1, '7701234567896', 269900.00, TRUE),-- Taladro Percutor Bosch 650W
(7, 7, 6, 4, 4, '7701234567897', 42500.00, TRUE), -- Sika 1 Impermeabilizante 4 Kg
(8, 8, 5, 1, 1, '7701234567898', 54000.00, TRUE), -- Pala Redonda Bellota No. 4
(9, 9, 7, 2, 5, '7701234567899', 6200.00, TRUE),  -- Disco Corte Metal Bosch
(10, 10, 2, 1, 1, '7701234567800', 28900.00, TRUE),-- Cinta Métrica Stanley 5m
(11, 11, 10, 8, 6, '7701234567801', 34000.00, TRUE),-- Cemento Gris Acesco Bulto 50kg
(12, 12, 2, 1, 1, '7701234567802', 62000.00, TRUE),-- Llave Stilson Stanley 12"
(13, 13, 3, 4, 4, '7701234567803', 76500.00, TRUE),-- Anticorrosivo Rojo Pintuco Galón
(14, 14, 4, 1, 1, '7701234567804', 36800.00, TRUE),-- Soldadura Líquida PVC Pavco 1/4 Gal
(15, 15, 5, 1, 1, '7701234567805', 19500.00, TRUE) -- Guante Vaqueta Bellota Par
ON DUPLICATE KEY UPDATE codigo_ean=VALUES(codigo_ean), tarifa_publico=VALUES(tarifa_publico), habilitado=VALUES(habilitado);

-- 8. SALDOS INICIALES DE INVENTARIO
INSERT INTO almacen_nivel (id, ficha_id, volumen_libre, coste_medio_ponderado, version) VALUES
(1, 1, 25.000, 26000.0000, 0),
(2, 2, 60.000, 10500.0000, 0),
(3, 3, 18.000, 61000.0000, 0),
(4, 4, 40.000, 14800.0000, 0),
(5, 5, 15.000, 31500.0000, 0),
(6, 6, 8.000, 198000.0000, 0),
(7, 7, 22.000, 29000.0000, 0),
(8, 8, 16.000, 37500.0000, 0),
(9, 9, 120.000, 3900.0000, 0),
(10, 10, 30.000, 19200.0000, 0),
(11, 11, 75.000, 26800.0000, 0),
(12, 12, 12.000, 43500.0000, 0),
(13, 13, 14.000, 53000.0000, 0),
(14, 14, 25.000, 25200.0000, 0),
(15, 15, 45.000, 12800.0000, 0)
ON DUPLICATE KEY UPDATE volumen_libre=VALUES(volumen_libre), coste_medio_ponderado=VALUES(coste_medio_ponderado);

-- 9. MOVIMIENTOS KÁRDEX DE INICIALIZACIÓN
INSERT INTO almacen_asiento (id, ficha_id, tipo_asiento, volumen, coste_individual_asiento, nivel_obtenido, folio_tipo, folio_id, causa, operario_id) VALUES
(1, 1, 'INICIALIZACION', 25.000, 26000.0000, 25.000, 'REAJUSTE', 1, 'Carga inicial de inventario', 1),
(2, 2, 'INICIALIZACION', 60.000, 10500.0000, 60.000, 'REAJUSTE', 2, 'Carga inicial de inventario', 1),
(3, 3, 'INICIALIZACION', 18.000, 61000.0000, 18.000, 'REAJUSTE', 3, 'Carga inicial de inventario', 1),
(4, 4, 'INICIALIZACION', 40.000, 14800.0000, 40.000, 'REAJUSTE', 4, 'Carga inicial de inventario', 1),
(5, 5, 'INICIALIZACION', 15.000, 31500.0000, 15.000, 'REAJUSTE', 5, 'Carga inicial de inventario', 1),
(6, 6, 'INICIALIZACION', 8.000, 198000.0000, 8.000, 'REAJUSTE', 6, 'Carga inicial de inventario', 1),
(7, 7, 'INICIALIZACION', 22.000, 29000.0000, 22.000, 'REAJUSTE', 7, 'Carga inicial de inventario', 1),
(8, 8, 'INICIALIZACION', 16.000, 37500.0000, 16.000, 'REAJUSTE', 8, 'Carga inicial de inventario', 1),
(9, 9, 'INICIALIZACION', 120.000, 3900.0000, 120.000, 'REAJUSTE', 9, 'Carga inicial de inventario', 1),
(10, 10, 'INICIALIZACION', 30.000, 19200.0000, 30.000, 'REAJUSTE', 10, 'Carga inicial de inventario', 1),
(11, 11, 'INICIALIZACION', 75.000, 26800.0000, 75.000, 'REAJUSTE', 11, 'Carga inicial de inventario', 1),
(12, 12, 'INICIALIZACION', 12.000, 43500.0000, 12.000, 'REAJUSTE', 12, 'Carga inicial de inventario', 1),
(13, 13, 'INICIALIZACION', 14.000, 53000.0000, 14.000, 'REAJUSTE', 13, 'Carga inicial de inventario', 1),
(14, 14, 'INICIALIZACION', 25.000, 25200.0000, 25.000, 'REAJUSTE', 14, 'Carga inicial de inventario', 1),
(15, 15, 'INICIALIZACION', 45.000, 12800.0000, 45.000, 'REAJUSTE', 15, 'Carga inicial de inventario', 1)
ON DUPLICATE KEY UPDATE id=VALUES(id);

-- 10. TERCEROS (CLIENTES Y PROVEEDORES SINTÉTICOS - RN-10)
INSERT INTO socio (id, tipo_identificacion, nro_identificacion, nombre_legal, tipo_persona, celular, email, ubicacion, es_comprador, es_suministrador, habilitado) VALUES
(1, 'NIT', '900543210-1', 'Distribuidora Ferretera Nacional S.A.S.', 'EMPRESA', '6013204500', 'ventas@ferrenacional.local', 'Zona Industrial Bogotá', FALSE, TRUE, TRUE),
(2, 'NIT', '890101456-2', 'Compañía Colombiana de Pinturas Pintuco S.A.', 'EMPRESA', '6044445566', 'pedidos@pintuco.local', 'Medellín, Antioquia', FALSE, TRUE, TRUE),
(3, 'NIT', '860012345-9', 'Pavco Wavin Colombia S.A.S.', 'EMPRESA', '6017823344', 'atencion@pavcowavin.local', 'Autopista Sur Km 11, Soacha', FALSE, TRUE, TRUE),
(4, 'NIT', '901056789-3', 'Bosch Herramientas y Maquinaria Colombia S.A.S.', 'EMPRESA', '6026543210', 'distribuidores@bosch.local', 'Zona Franca del Pacífico, Cali', FALSE, TRUE, TRUE),
(5, 'CC', '1020304050', 'Juan Carlos Pérez Rodríguez', 'NATURAL', '3109876543', 'juan.perez@constructor.local', 'Carrera 15 # 45-20, Bogotá', TRUE, FALSE, TRUE),
(6, 'CC', '1098765432', 'María Fernanda López Gómez', 'NATURAL', '3201234567', 'maria.lopez@arquitectura.local', 'Calle 80 # 24-10, Bogotá', TRUE, FALSE, TRUE),
(7, 'CC', '1018456123', 'Andrés Felipe Gómez Sánchez', 'NATURAL', '3157896541', 'andres.plomero@servicios.local', 'Calle 134 # 19-45, Bogotá', TRUE, FALSE, TRUE),
(8, 'CC', '1032789456', 'Claudia Marcela Restrepo Pardo', 'NATURAL', '3114567890', 'claudia.restrepo@hogar.local', 'Carrera 7 # 67-12, Bogotá', TRUE, FALSE, TRUE),
(9, 'NIT', '901234567-8', 'Construcciones y Remodelaciones del Norte S.A.S.', 'EMPRESA', '6017890123', 'admin@constructora.local', 'Av. Boyacá # 120-15, Bogotá', TRUE, TRUE, TRUE),
(10, 'NIT', '900876543-4', 'Ingeniería y Estructuras Metálicas de la Sabana S.A.S.', 'EMPRESA', '6018612345', 'compras@estructurasabana.local', 'Variante Chía - Cota Km 3', TRUE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE tipo_identificacion=VALUES(tipo_identificacion), nro_identificacion=VALUES(nro_identificacion), nombre_legal=VALUES(nombre_legal), tipo_persona=VALUES(tipo_persona), celular=VALUES(celular), email=VALUES(email), ubicacion=VALUES(ubicacion), es_comprador=VALUES(es_comprador), es_suministrador=VALUES(es_suministrador), habilitado=VALUES(habilitado);

-- 11. INICIALIZACIÓN DE CONSECUTIVOS DE COMPROBANTES (RN-12)
INSERT INTO tiquete_correlativo (id, tipo_identificacion, prefijo, ultimo_nro) VALUES
(1, 'EXPENDIO', 'EXP-', 100),
(2, 'ABASTECIMIENTO', 'CMP-', 50),
(3, 'RESTITUCION', 'DEV-', 10),
(4, 'REAJUSTE', 'AJU-', 5)
ON DUPLICATE KEY UPDATE tipo_identificacion=VALUES(tipo_identificacion);

SET FOREIGN_KEY_CHECKS = 1;
