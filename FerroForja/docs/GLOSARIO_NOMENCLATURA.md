# Glosario de Nomenclatura — FerroForja v2.0

Este documento registra la equivalencia entre la nomenclatura de la versión original y la del remake.
**Las reglas de negocio, los requisitos y los roles (`ADMINISTRADOR`, `USUARIO_OPERATIVO`) no cambian.**
El texto visible para el usuario final (etiquetas, mensajes) conserva el vocabulario del negocio
(«Venta», «Compra», «Cliente»…); lo que cambia es el **código**: clases, variables, tablas, columnas, rutas y paquetes.

## 1. Entidades y clases de dominio
| Concepto original | Nombre en el código |
| :--- | :--- |
| `Producto` | `Articulo` |
| `Marca` | `Fabricante` |
| `Presentación` | `Empaque` |
| `Referencia` | `Ficha` |
| `Unidad de medida` | `MedidaBase` |
| `Tercero` | `Socio` |
| `Cliente` | `Comprador` |
| `Proveedor` | `Suministrador` |
| `Usuario` | `Operario` |
| `Rol` | `Perfil` |
| `Inventario (módulo)` | `Almacen` |
| `Saldo de inventario` | `AlmacenNivel` |
| `Movimiento de Kárdex` | `AlmacenAsiento` |
| `Ajuste de inventario` | `ReajusteAlmacen` |
| `Venta` | `Expendio` |
| `Línea de venta` | `ExpendioDetalle` |
| `Compra` | `Abastecimiento` |
| `Línea de compra` | `AbastecimientoDetalle` |
| `Devolución` | `Restitucion` |
| `Línea de devolución` | `RestitucionDetalle` |
| `Comprobante` | `Tiquete` |
| `Consecutivo` | `Correlativo` |
| `Auditoría (log)` | `BitacoraLog` |
| `Documento (módulo)` | `Folio` |

## 2. Campos / columnas (aplican igual a JSON, Java, SQL y frontend)
| Original | Remake |
| :--- | :--- |
| `cantidad` | `volumen` |
| `cantidad_disponible` | `volumen_libre` |
| `costo_promedio_ponderado` | `coste_medio_ponderado` |
| `costo_unitario` | `coste_individual` |
| `precio_venta` | `tarifa_publico` |
| `precio_unitario` | `tarifa_individual` |
| `codigo_barras` | `codigo_ean` |
| `numero_documento` | `nro_identificacion` |
| `tipo_documento` | `tipo_identificacion` |
| `razon_social` | `nombre_legal` |
| `nombre` | `denominacion` |
| `nombre_completo` | `nombre_visible` |
| `descripcion` | `resena` |
| `activo` | `habilitado` |
| `estado` | `situacion` |
| `motivo_cancelacion` | `causa_anulacion` |
| `fecha_hora` | `marca_tiempo` |
| `fecha_creacion` | `creado_en` |
| `fecha_actualizacion` | `actualizado_en` |
| `monto_pagado_efectivo` | `importe_abonado_efectivo` |
| `cambio_devuelto` | `vuelto_entregado` |
| `telefono` | `celular` |
| `direccion` | `ubicacion` |
| `consecutivo` | `correlativo` |
| `stock (campos propios)` | `existencia` |

## 3. Verbos de métodos
| Original | Remake |
| :--- | :--- |
| `obtener…` | `recuperar…` |
| `listar…` | `enumerar…` |
| `registrar…` | `asentar…` |
| `crear…` | `generar…` |
| `actualizar…` | `modificar…` |
| `cancelar…` | `anular…` |
| `descontar…` | `restar…` |
| `reintegrar…` | `reponer…` |

## 4. Rutas REST
| Original | Remake |
| :--- | :--- |
| `/api/v1/sales` | `/api/v1/expendios` |
| `/api/v1/purchases` | `/api/v1/abastecimientos` |
| `/api/v1/returns` | `/api/v1/restituciones` |
| `/api/v1/catalog` | `/api/v1/catalogo` |
| `/api/v1/inventory` | `/api/v1/almacen` |
| `/api/v1/third-parties` | `/api/v1/socios` |
| `/api/v1/documents` | `/api/v1/folios` |
| `/api/v1/audit` | `/api/v1/bitacora` |
| `/api/v1/users` | `/api/v1/operarios` |

## 5. Paquetes Java (`co.ferroforja.bodega.*`)
| Original (`com.ferreteria.inventario.*`) | Remake |
| :--- | :--- |
| `auth` | `acceso` |
| `user` | `operarios` |
| `catalog` | `catalogo` |
| `customer` | `socios` |
| `document` | `folios` |
| `inventory` | `almacen` |
| `purchase` | `abastecimiento` |
| `returns` | `restituciones` |
| `sale` | `expendio` |
| `security` | `seguridad` |
| `shared` | `comun` |
| `supplier` | `suministro` |
| `reporting` | `reportes` |
| `config` | `configuracion` |
| `audit` | `bitacora` |

## 6. Credenciales del sistema
| Rol | Usuario | Contraseña | Perfil / Alcance |
| :--- | :--- | :--- | :--- |
| Gerente General | `gerente_forja` | `Forja#2026Master!` | Control administrativo total |
| Jefe de Almacén | `jefe_almacen` | `Bodega#2026Segura!` | Control de inventario y compras |
| Cajera Mostrador | `cajero_mostrador` | `Caja#2026Mostrador!` | Punto de venta y atención al cliente |
| Administrador Técnico | `admin_forja` | `Forja#Admin26` | Cuenta de soporte y fallback |

## 7. Identidad visual
* Paleta: amarillo de seguridad `#FFC400`, negro de taller `#141414`, concreto `#ECEAE4`.
* Tipografía: *Barlow Condensed* (títulos y botones), *Barlow* (texto), *JetBrains Mono* (cifras). Se empaquetan con `@fontsource` (funciona sin internet).
* Componentes: barra lateral negra con franja de peligro, tablas con cabecera negra/amarilla, tarjetas con borde grueso y sombra dura, formularios cuadrados.
* El tema completo está en `frontend/src/nucleo/theme/theme.js` y los estilos utilitarios en `frontend/src/estilos/global.css`.
