# Documento: Diseño de Contratos de la API REST

## Metadatos
* **Nombre:** 04_diseno_api_rest.md
* **Descripción:** Catálogo completo de endpoints REST, DTOs de entrada y salida, validaciones, códigos HTTP, roles requeridos, efectos sobre el inventario y manejo de errores mediante ProblemDetail (RFC 9457).
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto de API REST y Backend)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Convenciones REST y Estándares de Comunicación
* **URL Base:** `/api/v1`
* **Formato de Carga Útil:** JSON (`application/json;charset=UTF-8`).
* **Manejo de Errores:** Estándar **RFC 9457** (`application/problem+json`) mediante `org.springframework.http.ProblemDetail`.
* **Convención de Envoltorios:** **No se utilizará un envoltorio universal obligatorio** (como `ApiResponse<T>`). Las respuestas exitosas devuelven directamente el recurso, arreglo o estructura de paginación de Spring (`Page<T>`), apoyándose en los códigos de estado HTTP nativos.
* **Formatos de Fecha:** ISO-8601 (`YYYY-MM-DDTHH:mm:ss.sssZ`).
* **Paginación y Filtros:** Parámetros estándar: `page` (0-indexed, default 0), `size` (default 20, max 100), `sort` (campo,asc|desc).

---

## 2. Formato de Errores ProblemDetail (RFC 9457)
Toda respuesta de error (4xx y 5xx) sigue la siguiente estructura JSON canónica:

```json
{
  "type": "https://ferroforja.local/errors/stock-insuficiente",
  "title": "Stock Insuficiente",
  "status": 409,
  "detail": "La cantidad solicitada (5.000) excede el saldo disponible actual (2.000) para el producto Tornillo 2 pulg",
  "instance": "/api/v1/expendios",
  "timestamp": "2026-10-13T21:30:00Z",
  "invalidParams": [
    {
      "field": "lineas[0].cantidad",
      "reason": "Existencias insuficientes para completar la salida"
    }
  ]
}
```

---

## 3. Catálogo Completo de Endpoints REST por Módulo

### 3.1 Módulo: Autenticación y Sesión (`/api/v1/auth`)

#### **POST /api/v1/auth/login**
* **Descripción:** Autentica credenciales de usuario y crea una sesión web en el servidor respaldada por cookie `JSESSIONID`.
* **Rol requerido:** Público (anónimo).
* **DTO Solicitud (`LoginRequest`):**
  * `username` (`String`, obligatorio, no vacío).
  * `password` (`String`, obligatorio, no vacío).
* **DTO Respuesta (`AccesoResponse`):**
  * `id` (`Long`), `username` (`String`), `nombreVisible` (`String`), `perfiles` (`List<String>`).
* **Códigos HTTP:** 200 OK (éxito), 400 Bad Request (validación), 401 Unauthorized (credenciales incorrectas).
* **Efectos:** Genera cookie de sesión `JSESSIONID` con atributos `HttpOnly`, `Path=/`, `SameSite=Lax`.

#### **POST /api/v1/auth/logout**
* **Descripción:** Cierra la sesión activa en el servidor e invalida la cookie.
* **Rol requerido:** Autenticado (`ADMINISTRADOR`, `USUARIO_OPERATIVO`).
* **Códigos HTTP:** 204 No Content.
* **Efectos:** Invalida `HttpSession` en el servidor y expira la cookie.

#### **GET /api/v1/auth/me**
* **Descripción:** Obtiene los datos del usuario en la sesión activa actual para verificar estado de autenticación al recargar la interfaz.
* **Rol requerido:** Autenticado.
* **DTO Respuesta:** `AccesoResponse`.
* **Códigos HTTP:** 200 OK, 401 Unauthorized.

---

### 3.2 Módulo: Catálogo de Productos (`/api/v1/catalogo`)

#### **GET /api/v1/catalog/references**
* **Descripción:** Consulta paginada y filtrable de referencias de productos.
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Parámetros de Consulta:** `search` (código de barras, nombre), `habilitado` (booleano, default true), `page`, `size`, `sort`.
* **DTO Respuesta (`Page<FichaResponse>`):**
  * `id`, `codigoEan`, `articuloDenominacion`, `fabricanteDenominacion`, `empaqueDenominacion`, `medidaBase`, `tarifaPublico`, `habilitado`, `existenciaLibre`.
* **Códigos HTTP:** 200 OK.

#### **POST /api/v1/catalog/references**
* **Descripción:** Registra una nueva referencia comercial en el catálogo.
* **Rol requerido:** `ADMINISTRADOR`.
* **DTO Solicitud (`GenerarFichaRequest`):**
  * `articuloId` (`Long`, obligatorio).
  * `fabricanteId` (`Long`, obligatorio).
  * `empaqueId` (`Long`, obligatorio).
  * `medidaBaseId` (`Long`, obligatorio).
  * `codigoEan` (`String`, obligatorio, longitud 3-50, sin espacios).
  * `tarifaPublico` (`BigDecimal`, obligatorio, min 0.01).
* **DTO Respuesta:** `FichaResponse`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 409 Conflict (código de barras o tupla duplicada).
* **Efectos:** Crea referencia con `habilitado=true` y crea automáticamente registro en `almacen_nivel` con saldo 0.000 y CPP 0.0000.

#### **PATCH /api/v1/catalog/references/{id}/price**
* **Descripción:** Actualiza el precio de venta de una referencia y asienta auditoría obligatoria.
* **Rol requerido:** `ADMINISTRADOR` o `USUARIO_OPERATIVO` (con permiso de inventario).
* **DTO Solicitud (`ModificarTarifaRequest`):**
  * `nuevoTarifa` (`BigDecimal`, obligatorio, min 0.01).
  * `causa` (`String`, opcional).
* **DTO Respuesta:** `FichaResponse`.
* **Códigos HTTP:** 200 OK, 400 Bad Request, 404 Not Found.
* **Efectos:** Actualiza precio en catálogo y registra evento en `bitacora_log` (RN-09, RN-11).

#### **DELETE /api/v1/catalog/references/{id}**
* **Descripción:** Desactiva lógicamente una referencia (o borra físicamente solo si carece totalmente de historial).
* **Rol requerido:** `ADMINISTRADOR`.
* **Códigos HTTP:** 204 No Content, 404 Not Found, 409 Conflict.
* **Efectos:** Si tiene historial pasa `habilitado = false` (RN-02).

---

### 3.3 Módulo: Terceros - Clientes y Proveedores (`/api/v1/socios`)

#### **GET /api/v1/third-parties**
* **Descripción:** Consulta paginada y filtrable de terceros.
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Parámetros:** `tipo` (`COMPRADOR`, `SUMINISTRADOR`, `TODOS`), `search` (documento o nombre), `page`, `size`.
* **DTO Respuesta:** `Page<SocioResponse>`.
* **Códigos HTTP:** 200 OK.

#### **POST /api/v1/third-parties**
* **Descripción:** Registra un nuevo cliente o proveedor unificado (RN-10).
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **DTO Solicitud (`GenerarSocioRequest`):**
  * `tipoIdentificacion` (`String`, CC, NIT, CE, etc.).
  * `nroIdentificacion` (`String`, obligatorio, longitud 4-30).
  * `nombreLegal` (`String`, obligatorio, longitud 2-150).
  * `tipoPersona` (`String`, `NATURAL` | `EMPRESA`).
  * `celular` (`String`, opcional), `email` (`String`, opcional), `ubicacion` (`String`, opcional).
  * `esComprador` (`Boolean`, obligatorio), `esSuministrador` (`Boolean`, obligatorio).
* **DTO Respuesta:** `SocioResponse`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 409 Conflict (documento ya registrado).

---

### 3.4 Módulo: Inventario y Movimientos (`/api/v1/almacen`)

#### **GET /api/v1/inventory/stocks**
* **Descripción:** Consulta del saldo actual disponible y costo promedio ponderado de todas las referencias.
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO` (el campo `costeMedio` es nulo para operadores no autorizados).
* **Parámetros:** `search`, `existenciaBaja` (booleano), `page`, `size`.
* **DTO Respuesta:** `Page<AlmacenNivelResponse>`.
* **Códigos HTTP:** 200 OK.

#### **GET /api/v1/inventory/references/{id}/movements**
* **Descripción:** Consulta del Kárdex (movimientos cronológicos inmutables) de una referencia.
* **Rol requerido:** `ADMINISTRADOR`.
* **Parámetros:** `desde`, `hasta`, `page`, `size`.
* **DTO Respuesta:** `Page<AlmacenAsientoResponse>`.
* **Códigos HTTP:** 200 OK, 404 Not Found.

#### **POST /api/v1/inventory/initial**
* **Descripción:** Asienta el inventario inicial de una referencia con saldo cero (sin compras simuladas).
* **Rol requerido:** `ADMINISTRADOR` (exclusivo).
* **DTO Solicitud (`AlmacenAperturaRequest`):**
  * `fichaId` (`Long`, obligatorio).
  * `volumenApertura` (`BigDecimal`, obligatorio, min 0.001).
  * `costeIndividual` (`BigDecimal`, obligatorio, min 0.01).
* **DTO Respuesta:** `AlmacenNivelResponse`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 409 Conflict (la referencia ya tiene saldo o movimientos).
* **Efectos:** Fija saldo inicial, fija CPP inicial, genera movimiento `INICIALIZACION` y audita la acción.

#### **POST /api/v1/inventory/adjustments**
* **Descripción:** Ejecuta un ajuste manual de inventario (físico vs sistema) con motivo documentado.
* **Rol requerido:** `ADMINISTRADOR` (exclusivo - RN-08).
* **DTO Solicitud (`ReajusteAlmacenRequest`):**
  * `fichaId` (`Long`, obligatorio).
  * `tipoReajuste` (`String`, `INCREMENTO` | `DISMINUCION`).
  * `volumen` (`BigDecimal`, obligatorio, min 0.001).
  * `causa` (`String`, obligatorio, longitud 5-255).
* **DTO Respuesta:** `ReajusteAlmacenResponse`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 403 Forbidden, 409 Conflict (disminución excede stock).
* **Efectos:** Actualiza saldo disponible, genera movimiento de Kárdex y registra entrada en auditoría.

---

### 3.5 Módulo: Compras (`/api/v1/abastecimientos`)

#### **POST /api/v1/purchases**
* **Descripción:** Registra y recibe en un solo paso una compra completa a proveedor (RN-03).
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **DTO Solicitud (`GenerarAbastecimientoRequest`):**
  * `suministradorId` (`Long`, obligatorio).
  * `detalles` (`List<AbastecimientoDetalleRequest>`, min 1 elemento):
    * `fichaId` (`Long`, obligatorio).
    * `volumen` (`BigDecimal`, obligatorio, min 0.001).
    * `costeIndividual` (`BigDecimal`, obligatorio, min 0.01).
* **DTO Respuesta (`AbastecimientoResponse`):**
  * `id`, `correlativo`, `suministradorId`, `marcaTiempo`, `total`, `situacion`, `detalles` (con subtotales calculados).
* **Códigos HTTP:** 201 Created, 400 Bad Request, 404 Not Found (proveedor o producto inexistente).
* **Efectos Transaccionales:**
  * Crea cabecera `CMP-XXXXXX` y líneas.
  * Por cada línea: incrementa stock disponible, genera movimiento de Kárdex `ABASTECIMIENTO` y recalcula CPP (RN-04).

#### **POST /api/v1/purchases/{id}/cancel**
* **Descripción:** Cancela una compra con motivo obligatorio, reintegrando el inventario si las existencias actuales lo permiten.
* **Rol requerido:** `ADMINISTRADOR` (exclusivo).
* **DTO Solicitud (`AnularFolioRequest`):**
  * `causa` (`String`, obligatorio, min 5 caracteres).
* **DTO Respuesta:** `AbastecimientoResponse`.
* **Códigos HTTP:** 200 OK, 400 Bad Request, 403 Forbidden, 409 Conflict (stock insuficiente para revertir).

---

### 3.6 Módulo: Ventas de Contado (`/api/v1/expendios`)

#### **POST /api/v1/sales**
* **Descripción:** Registra una venta de contado en efectivo obligatoriamente vinculada a un cliente (RN-05, RN-06).
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **DTO Solicitud (`GenerarExpendioRequest`):**
  * `compradorId` (`Long`, obligatorio).
  * `importeEfectivoRecibido` (`BigDecimal`, obligatorio).
  * `detalles` (`List<ExpendioDetalleRequest>`, min 1 elemento):
    * `fichaId` (`Long`, obligatorio).
    * `volumen` (`BigDecimal`, obligatorio, min 0.001).
    * *(Nota de seguridad: el cliente NO envía precios, costos ni totales; el backend los calcula del catálogo y saldos).*
* **DTO Respuesta (`ExpendioResponse`):**
  * `id`, `correlativo` (`EXP-XXXXXX`), `compradorDenominacion`, `marcaTiempo`, `subtotal`, `total`, `importeEfectivoRecibido`, `vueltoEntregado`, `situacion`, `detalles`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 404 Not Found, 409 Conflict (stock insuficiente por RN-01 o concurrencia).
* **Efectos Transaccionales:**
  * Bloqueo atómico de las referencias afectadas.
  * Verificación de stock disponible >= cantidad solicitada.
  * Congelamiento del costo histórico vigente (`coste_congelado_individual`) en cada línea (RN-04).
  * Descuento de stock en `almacen_nivel`.
  * Generación de movimientos de Kárdex `EXPENDIO`.
  * Emisión de consecutivo correlativo `EXP-XXXXXX`.

#### **POST /api/v1/sales/{id}/cancel**
* **Descripción:** Cancela una venta con motivo obligatorio, devolviendo las unidades al inventario.
* **Rol requerido:** `ADMINISTRADOR` (exclusivo).
* **DTO Solicitud:** `AnularFolioRequest`.
* **Códigos HTTP:** 200 OK, 403 Forbidden, 409 Conflict (venta con devoluciones asociadas).

---

### 3.7 Módulo: Devoluciones (`/api/v1/restituciones`)

#### **POST /api/v1/returns**
* **Descripción:** Registra una devolución de cliente (vinculada a venta o libre) y reincorpora existencias a stock (RN-07).
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **DTO Solicitud (`GenerarRestitucionRequest`):**
  * `expendioId` (`Long`, opcional).
  * `compradorId` (`Long`, opcional si viene venta).
  * `causa` (`String`, obligatorio, longitud 5-255).
  * `detalles` (`List<RestitucionDetalleRequest>`, min 1 elemento):
    * `fichaId` (`Long`, obligatorio).
    * `volumen` (`BigDecimal`, obligatorio, min 0.001).
* **DTO Respuesta (`RestitucionResponse`):**
  * `id`, `correlativo` (`DEV-XXXXXX`), `tipoRestitucion`, `marcaTiempo`, `detalles`.
* **Códigos HTTP:** 201 Created, 400 Bad Request, 409 Conflict (cantidad a devolver excede lo vendido en la venta original).
* **Efectos:** Reingresa stock al inventario; si es vinculada usa el costo histórico de la venta original, si es libre usa el CPP vigente (RN-04, RN-07).

---

### 3.8 Módulo: Documentos y Comprobantes (`/api/v1/folios`)

#### **GET /api/v1/documents/{tipo}/{consecutivo}**
* **Descripción:** Obtiene los datos estructurados de un comprobante interno para visualización e impresión.
* **Rol requerido:** `ADMINISTRADOR`, `USUARIO_OPERATIVO`.
* **Parámetros de Ruta:** `tipo` (`sales`, `purchases`, `returns`, `adjustments`), `correlativo` (ej. `EXP-000001`).
* **DTO Respuesta:** `FolioTiqueteResponse` (datos de ferretería, cliente/proveedor, líneas, importes en COP).
* **Códigos HTTP:** 200 OK, 404 Not Found.

---

### 3.9 Módulo: Auditoría (`/api/v1/bitacora`)

#### **GET /api/v1/audit/logs**
* **Descripción:** Consulta paginada y filtrable de la bitácora inmutable de auditoría (RN-11).
* **Rol requerido:** `ADMINISTRADOR` (exclusivo).
* **Parámetros:** `modulo`, `operarioId`, `desde`, `hasta`, `page`, `size`.
* **DTO Respuesta:** `Page<BitacoraLogResponse>`.
* **Códigos HTTP:** 200 OK, 403 Forbidden.
