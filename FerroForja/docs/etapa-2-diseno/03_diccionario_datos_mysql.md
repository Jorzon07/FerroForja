# Documento: Diccionario de Datos MySQL 8.0

## Metadatos
* **Nombre:** 03_diccionario_datos_mysql.md
* **Descripción:** Diccionario de datos exhaustivo para MySQL 8.0, especificando tablas, columnas, tipos de datos SQL, restricciones (PK, FK, UK, CHECK), índices y justificaciones de diseño.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Especialista en Bases de Datos y MySQL)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Convenciones y Estándares de Base de Datos
* **Nomenclatura:** Tablas y columnas en minúsculas con guiones bajos (`snake_case`).
* **Representación Monetaria (COP):** `DECIMAL(15, 2)` con redondeo estándar a 2 decimales. Queda **prohibido el uso de FLOAT o DOUBLE**.
* **Representación de Cantidades Físicas:** `DECIMAL(12, 3)` para admitir unidades enteras y fracciones de hasta 3 decimales (metros, kilos, galones).
* **Representación de Costo Promedio (CPP):** `DECIMAL(15, 4)` para preservar precisión matemática y evitar acumulación de errores de redondeo.
* **Marcas temporales:** `DATETIME(6)` para alta precisión en auditoría y Kárdex.
* **Motor:** `InnoDB` con juego de caracteres `utf8mb4` y cotejamiento `utf8mb4_unicode_ci`.

---

## 2. Tablas del Módulo: Identidad y Seguridad

### 2.1 Tabla: `operario`
* **Responsabilidad:** Credenciales y estado de las cuentas de usuario con acceso al sistema web.
* **Motor:** InnoDB.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador primario subrogado. |
| `username` | `VARCHAR(50)` | NO | **UK** `uk_operario_username` | Nombre de usuario único para login. |
| `password_hash` | `VARCHAR(100)` | NO | | Hash seguro generado con BCrypt. |
| `nombre_visible`| `VARCHAR(120)` | NO | | Nombre y apellidos del usuario. |
| `email` | `VARCHAR(100)` | SÍ | | Correo electrónico de contacto. |
| `habilitado` | `BOOLEAN` | NO | DEFAULT `TRUE` | Estado de la cuenta (borrado lógico). |
| `creado_en` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha y hora de alta. |
| `actualizado_en` | `DATETIME(6)` | SÍ | | Fecha y hora del último cambio. |

* **Índices:** `idx_operario_username` (B-Tree sobre `username`).

---

### 2.2 Tabla: `perfil`
* **Responsabilidad:** Catálogo de roles de autorización reconocidos por Spring Security.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del rol. |
| `denominacion` | `VARCHAR(40)` | NO | **UK** `uk_perfil_denominacion` | Código del rol: `ROLE_ADMINISTRADOR`, `ROLE_USUARIO_OPERATIVO`. |
| `resena` | `VARCHAR(150)` | SÍ | | Descripción funcional del rol. |

---

### 2.3 Tabla: `operario_perfil`
* **Responsabilidad:** Tabla asociativa muchos a muchos entre usuarios y roles.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `operario_id` | `BIGINT` | NO | **PK**, **FK** `operario(id)` ON DELETE CASCADE | ID del usuario. |
| `perfil_id` | `BIGINT` | NO | **PK**, **FK** `perfil(id)` ON DELETE RESTRICT | ID del rol asignado. |

---

## 3. Tablas del Módulo: Catálogo y Terceros

### 3.1 Tabla: `socio`
* **Responsabilidad:** Información unificada de clientes y proveedores (personas naturales o jurídicas).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del tercero. |
| `tipo_identificacion` | `VARCHAR(10)` | NO | CHECK (`tipo_identificacion` IN ('CC','NIT','CE','PASAPORTE')) | Tipo de documento fiscal colombiano. |
| `nro_identificacion`| `VARCHAR(30)` | NO | **UK** `uk_socio_folio` | Número de documento fiscal único. |
| `nombre_legal` | `VARCHAR(150)` | NO | | Nombre comercial o nombre de la persona. |
| `tipo_persona` | `VARCHAR(15)` | NO | CHECK (`tipo_persona` IN ('NATURAL','JURIDICA')) | Distinción fiscal. |
| `celular` | `VARCHAR(30)` | SÍ | | Teléfono o celular de contacto. |
| `email` | `VARCHAR(100)` | SÍ | | Correo de facturación / contacto. |
| `ubicacion` | `VARCHAR(150)` | SÍ | | Dirección física. |
| `es_comprador` | `BOOLEAN` | NO | DEFAULT `TRUE` | Bandera que habilita rol comercial cliente. |
| `es_suministrador` | `BOOLEAN` | NO | DEFAULT `FALSE` | Bandera que habilita rol comercial proveedor. |
| `habilitado` | `BOOLEAN` | NO | DEFAULT `TRUE` | Borrado lógico. |

* **Índices:** `idx_socio_folio` (`nro_identificacion`), `idx_socio_nombre_legal` (`nombre_legal`).

---

### 3.2 Tabla: `articulo`
* **Responsabilidad:** Concepto genérico del artículo (ej. Martillo de bola, Esmalte sintético).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del producto. |
| `denominacion` | `VARCHAR(100)` | NO | | Nombre genérico del artículo. |
| `resena` | `VARCHAR(255)` | SÍ | | Detalle técnico del producto. |
| `habilitado` | `BOOLEAN` | NO | DEFAULT `TRUE` | Estado lógico. |

---

### 3.3 Tabla: `fabricante`
* **Responsabilidad:** Fabricante o marca comercial (ej. Herragro, Stanley, Pintuco).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la marca. |
| `denominacion` | `VARCHAR(80)` | NO | **UK** `uk_fabricante_denominacion` | Nombre de la marca. |
| `habilitado` | `BOOLEAN` | NO | DEFAULT `TRUE` | Estado lógico. |

---

### 3.4 Tabla: `empaque`
* **Responsabilidad:** Empaque comercial (ej. Caja x 100, Galón, Unidad, Rollo 50m).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la presentación. |
| `denominacion` | `VARCHAR(80)` | NO | **UK** `uk_empaque_denominacion` | Nombre de la presentación comercial. |
| `habilitado` | `BOOLEAN` | NO | DEFAULT `TRUE` | Estado lógico. |

---

### 3.5 Tabla: `medida_base`
* **Responsabilidad:** Unidades de magnitud física estándar para el control de inventario.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador. |
| `clave` | `VARCHAR(10)` | NO | **UK** `uk_medida_clave` | Código estándar (UND, MTR, KGM, GLN). |
| `denominacion` | `VARCHAR(50)` | NO | | Nombre legible (Unidad, Metro, Kilo). |

---

### 3.6 Tabla: `ficha`
* **Responsabilidad:** Renglón vendible definitivo en el catálogo (Producto + Marca + Presentación).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la referencia vendible. |
| `articulo_id` | `BIGINT` | NO | **FK** `articulo(id)` ON DELETE RESTRICT | Producto base. |
| `fabricante_id` | `BIGINT` | NO | **FK** `fabricante(id)` ON DELETE RESTRICT | Marca fabricante. |
| `empaque_id`| `BIGINT` | NO | **FK** `empaque(id)` ON DELETE RESTRICT | Presentación comercial. |
| `medida_base_id`| `BIGINT` | NO | **FK** `medida_base(id)` ON DELETE RESTRICT| Unidad base. |
| `codigo_ean` | `VARCHAR(50)` | NO | **UK** `uk_fch_codigo_ean` | Código de barras único para venta/búsqueda. |
| `tarifa_publico` | `DECIMAL(15, 2)`| NO | CHECK (`tarifa_publico` >= 0) | Precio unitario al público en COP. |
| `habilitado` | `BOOLEAN` | NO | DEFAULT `TRUE` | Borrado lógico (RN-02). |
| `creado_en` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha de alta en catálogo. |

* **Restricción de tupla:** `UK` `uk_fch_prod_fabricante_emp` (`articulo_id`, `fabricante_id`, `empaque_id`).
* **Índices:** `idx_fch_codigo_ean`, `idx_fch_articulo`.

---

## 4. Tablas del Módulo: Inventario y Movimientos (Kárdex)

### 4.1 Tabla: `almacen_nivel`
* **Responsabilidad:** Registro atómico del stock disponible y costo promedio actual por referencia.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del registro de saldo. |
| `ficha_id` | `BIGINT` | NO | **UK**, **FK** `ficha(id)` ON DELETE RESTRICT | Referencia a la que pertenece el stock. |
| `volumen_libre`| `DECIMAL(12, 3)`| NO | DEFAULT 0.000, CHECK (`volumen_libre` >= 0) | Saldo físico actual en almacén (RN-01). |
| `coste_medio_ponderado`| `DECIMAL(15, 4)`| NO | DEFAULT 0.0000, CHECK (`coste_medio_ponderado` >= 0) | Costo de valoración vigente (RN-04). |
| `actualizado_en`| `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` ON UPDATE `CURRENT_TIMESTAMP(6)` | Última modificación de stock. |
| `version` | `BIGINT` | NO | DEFAULT 0 | Campo de versión para bloqueo optimista JPA. |

---

### 4.2 Tabla: `almacen_asiento`
* **Responsabilidad:** Historial cronológico inmutable (Kárdex) de cada transacción física.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del movimiento. |
| `ficha_id` | `BIGINT` | NO | **FK** `ficha(id)` ON DELETE RESTRICT | Referencia afectada. |
| `tipo_asiento`| `VARCHAR(30)` | NO | CHECK (`tipo_asiento` IN ('INICIALIZACION','COMPRA','VENTA','RESTITUCION_COMPRADOR','REAJUSTE_ENTRADA','REAJUSTE_SALIDA','ANULACION_EXPENDIO','ANULACION_ABASTECIMIENTO')) | Clasificación del flujo. |
| `volumen` | `DECIMAL(12, 3)`| NO | CHECK (`volumen` > 0) | Magnitud física movida. |
| `coste_individual_asiento`| `DECIMAL(15, 4)`| NO | CHECK (`coste_individual_asiento` >= 0) | Costo unitario aplicado en la operación. |
| `nivel_obtenido`| `DECIMAL(12, 3)`| NO | CHECK (`nivel_obtenido` >= 0) | Saldo en almacén tras el movimiento. |
| `folio_tipo` | `VARCHAR(20)` | NO | | `EXPENDIO`, `ABASTECIMIENTO`, `RESTITUCION`, `REAJUSTE`. |
| `folio_id` | `BIGINT` | NO | | ID del documento comercial origen. |
| `causa` | `VARCHAR(255)` | SÍ | | Justificación (obligatorio para ajustes). |
| `marca_tiempo` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Momento exacto del registro. |
| `operario_id` | `BIGINT` | NO | **FK** `operario(id)` ON DELETE RESTRICT | Usuario responsable de la transacción. |

* **Índices:** `idx_asi_fch_fecha` (`ficha_id`, `marca_tiempo`), `idx_asi_doc` (`folio_tipo`, `folio_id`).

---

## 5. Tablas de Operaciones Comerciales

### 5.1 Tabla: `abastecimiento`
* **Responsabilidad:** Cabecera de compra de mercancía a proveedores.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la compra. |
| `correlativo` | `VARCHAR(25)` | NO | **UK** `uk_abastecimiento_correlativo` | Consecutivo correlativo (ej. `CMP-000001`). |
| `suministrador_id` | `BIGINT` | NO | **FK** `socio(id)` ON DELETE RESTRICT | Proveedor registrado (RN-10). |
| `marca_tiempo` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha y hora de recepción. |
| `total` | `DECIMAL(15, 2)`| NO | CHECK (`total` >= 0) | Total liquidado de la compra en COP. |
| `situacion` | `VARCHAR(20)` | NO | CHECK (`situacion` IN ('RECIBIDA','CANCELADA')) | Estado del documento (RN-03). |
| `causa_anulacion`| `VARCHAR(255)`| SÍ | | Motivo documentado si se anula. |
| `operario_id` | `BIGINT` | NO | **FK** `operario(id)` ON DELETE RESTRICT | Usuario que registró la compra. |

---

### 5.2 Tabla: `abastecimiento_detalle`
* **Responsabilidad:** Detalle por renglón de artículos en la compra.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la línea. |
| `abastecimiento_id` | `BIGINT` | NO | **FK** `abastecimiento(id)` ON DELETE CASCADE | Compra a la que pertenece. |
| `ficha_id` | `BIGINT` | NO | **FK** `ficha(id)` ON DELETE RESTRICT | Producto adquirido. |
| `volumen` | `DECIMAL(12, 3)`| NO | CHECK (`volumen` > 0) | Unidades recibidas. |
| `coste_individual`| `DECIMAL(15, 4)`| NO | CHECK (`coste_individual` >= 0) | Costo de adquisición unitario. |
| `subtotal` | `DECIMAL(15, 2)`| NO | CHECK (`subtotal` >= 0) | Total de la línea en COP. |

---

### 5.3 Tabla: `expendio`
* **Responsabilidad:** Cabecera de venta de mostrador de contado.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la venta. |
| `correlativo` | `VARCHAR(25)` | NO | **UK** `uk_expendio_correlativo` | Consecutivo correlativo (ej. `EXP-000001`). |
| `comprador_id` | `BIGINT` | NO | **FK** `socio(id)` ON DELETE RESTRICT | Cliente registrado obligatorio (RN-05). |
| `marca_tiempo` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha y hora de expedición. |
| `subtotal` | `DECIMAL(15, 2)`| NO | CHECK (`subtotal` >= 0) | Subtotal en COP. |
| `total` | `DECIMAL(15, 2)`| NO | CHECK (`total` >= 0) | Total a pagar en COP. |
| `importe_abonado_efectivo`| `DECIMAL(15, 2)`| NO | CHECK (`importe_abonado_efectivo` >= `total`) | Monto entregado en efectivo (RN-06). |
| `vuelto_entregado`| `DECIMAL(15, 2)`| NO | DEFAULT 0.00 | Cambio entregado al cliente. |
| `situacion` | `VARCHAR(20)` | NO | CHECK (`situacion` IN ('COMPLETADA','CANCELADA')) | Estado de la venta. |
| `causa_anulacion`| `VARCHAR(255)`| SÍ | | Justificación en caso de cancelación. |
| `operario_id` | `BIGINT` | NO | **FK** `operario(id)` ON DELETE RESTRICT | Vendedor o cajero responsable. |

---

### 5.4 Tabla: `expendio_detalle`
* **Responsabilidad:** Renglón de productos vendidos, congelando precio y costo histórico.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del ítem vendido. |
| `expendio_id` | `BIGINT` | NO | **FK** `expendio(id)` ON DELETE CASCADE | Venta asociada. |
| `ficha_id` | `BIGINT` | NO | **FK** `ficha(id)` ON DELETE RESTRICT | Producto despachado. |
| `volumen` | `DECIMAL(12, 3)`| NO | CHECK (`volumen` > 0) | Cantidad despachada. |
| `tarifa_individual`| `DECIMAL(15, 2)`| NO | CHECK (`tarifa_individual` >= 0) | Precio unitario facturado en COP. |
| `coste_congelado_individual`| `DECIMAL(15, 4)`| NO | CHECK (`coste_congelado_individual` >= 0) | **Costo promedio vigente al momento de la venta** (RN-04). |
| `subtotal` | `DECIMAL(15, 2)`| NO | CHECK (`subtotal` >= 0) | `cantidad * tarifa_individual`. |

---

### 5.5 Tabla: `restitucion`
* **Responsabilidad:** Cabecera de devolución de cliente (vinculada o no vinculada).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la devolución. |
| `correlativo` | `VARCHAR(25)` | NO | **UK** `uk_dev_correlativo` | Consecutivo correlativo (ej. `DEV-000001`). |
| `expendio_id` | `BIGINT` | SÍ | **FK** `expendio(id)` ON DELETE RESTRICT | Venta de origen (nulo si no es vinculada). |
| `comprador_id` | `BIGINT` | SÍ | **FK** `socio(id)` ON DELETE RESTRICT | Cliente que devuelve. |
| `tipo_restitucion`| `VARCHAR(20)` | NO | CHECK (`tipo_restitucion` IN ('VINCULADA','NO_VINCULADA')) | Modalidad de la devolución (RN-07). |
| `marca_tiempo` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha y hora de recepción. |
| `causa` | `VARCHAR(255)` | NO | | Justificación de la devolución. |
| `situacion` | `VARCHAR(20)` | NO | DEFAULT 'APROBADA' | Estado del trámite. |
| `operario_id` | `BIGINT` | NO | **FK** `operario(id)` ON DELETE RESTRICT | Usuario que autorizó la devolución. |

---

### 5.6 Tabla: `restitucion_detalle`
* **Responsabilidad:** Detalle de productos devueltos y su costo de reingreso a inventario.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador de la línea. |
| `restitucion_id` | `BIGINT` | NO | **FK** `restitucion(id)` ON DELETE CASCADE | Documento de devolución. |
| `ficha_id` | `BIGINT` | NO | **FK** `ficha(id)` ON DELETE RESTRICT | Referencia reincorporada. |
| `volumen` | `DECIMAL(12, 3)`| NO | CHECK (`volumen` > 0) | Unidades devueltas al stock. |
| `coste_individual_aplicado`| `DECIMAL(15, 4)`| NO | | Costo con el que reingresa (histórico o promedio). |

---

### 5.7 Tabla: `reajuste_almacen`
* **Responsabilidad:** Registro de ajustes manuales de stock autorizados por el Administrador.

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del ajuste. |
| `correlativo` | `VARCHAR(25)` | NO | **UK** `uk_reajuste_correlativo` | Consecutivo correlativo (ej. `AJU-000001`). |
| `ficha_id` | `BIGINT` | NO | **FK** `ficha(id)` ON DELETE RESTRICT | Referencia ajustada. |
| `tipo_reajuste` | `VARCHAR(20)` | NO | CHECK (`tipo_reajuste` IN ('INCREMENTO','DISMINUCION')) | Sentido del ajuste físico. |
| `volumen` | `DECIMAL(12, 3)`| NO | CHECK (`volumen` > 0) | Unidades ajustadas. |
| `coste_individual_ficha`| `DECIMAL(15, 4)`| NO | | Costo de valoración del ajuste. |
| `causa` | `VARCHAR(255)` | NO | | Justificación obligatoria del ajuste (RN-08). |
| `marca_tiempo` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Momento del ajuste. |
| `operario_id` | `BIGINT` | NO | **FK** `operario(id)` ON DELETE RESTRICT | Administrador que ejecutó el ajuste. |

---

## 6. Tablas de Control Transversal

### 6.1 Tabla: `tiquete_correlativo`
* **Responsabilidad:** Control correlativo de numeraciones internas por tipo de comprobante (RN-12).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador. |
| `tipo_identificacion` | `VARCHAR(25)` | NO | **UK** `uk_correlativo_tipo` | `EXPENDIO`, `ABASTECIMIENTO`, `RESTITUCION`, `REAJUSTE`. |
| `prefijo` | `VARCHAR(10)` | NO | | Prefijo textual (`EXP-`, `CMP-`, `DEV-`, `AJU-`). |
| `ultimo_nro` | `BIGINT` | NO | DEFAULT 0 | Último correlativo asignado. |

---

### 6.2 Tabla: `bitacora_log`
* **Responsabilidad:** Registro básico inmutable de auditoría del sistema (RN-11).

| Columna | Tipo SQL | Nulo | Restricciones / Claves | Descripción |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | NO | **PK** | Identificador del log. |
| `operario_id` | `BIGINT` | SÍ | **FK** `operario(id)` ON DELETE SET NULL | Usuario responsable. |
| `marca_tiempo` | `DATETIME(6)` | NO | DEFAULT `CURRENT_TIMESTAMP(6)` | Fecha y hora exacta. |
| `modulo` | `VARCHAR(40)` | NO | | Módulo del sistema afectado. |
| `accion` | `VARCHAR(50)` | NO | | Acción ejecutada (MODIFICAR_TARIFA, CANCELAR, etc.). |
| `resena` | `TEXT` | NO | | Detalle explicativo de la operación. |

* **Índice:** `idx_bitacora_fecha` (`marca_tiempo`).
