# Documento: Catálogo Actualizado de Contratos de la API REST

## Metadatos
* **Nombre:** 02_contratos_api_rest_actualizados.md
* **Descripción:** Catálogo definitivo de endpoints REST, esquemas de entrada/salida (DTOs), reglas de validación, roles de seguridad y códigos de respuesta ProblemDetail (RFC 9457) implementados en el backend.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto de API REST y Backend)
* **Versión:** 1.0
* **Estado:** Aprobado

---

## 1. Convenciones REST y Estándares de la API
* **Prefijo Base:** `/api/v1`
* **Protocolo y Formato:** HTTP/1.1 y HTTP/2 sobre JSON (`application/json;charset=UTF-8`).
* **Manejo Estandarizado de Errores:** Spring Framework `ProblemDetail` conforme a **RFC 9457** (`application/problem+json`).
* **Autenticación:** Sesiones web mediante cookie de servidor `JSESSIONID` (`HttpOnly`, `SameSite=Lax`).
* **Seguridad CSRF:** Protección obligatoria para verbos mutativos (`POST`, `PUT`, `PATCH`, `DELETE`) vía cabecera `X-XSRF-TOKEN`.

---

## 2. Catálogo Detallado de Endpoints Implementados

### 2.1 Autenticación y Sesiones (`/api/v1/auth`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Anónimo | `LoginRequest` (`username`, `password`) | `AccesoResponse` | 200, 400, 401 |
| `POST` | `/api/v1/auth/logout` | Autenticado | N/A | N/A | 204 |
| `GET` | `/api/v1/auth/me` | Autenticado | N/A | `AccesoResponse` | 200, 401 |

### 2.2 Gestión de Usuarios (`/api/v1/operarios`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/operarios` | `ADMINISTRADOR` | Query: `page`, `size` | `Page<OperarioResponse>` | 200, 403 |
| `POST` | `/api/v1/operarios` | `ADMINISTRADOR` | `GenerarOperarioRequest` | `OperarioResponse` | 201, 400, 409 |
| `PATCH`| `/api/v1/operarios/{id}/conmutar-situacion` | `ADMINISTRADOR` | N/A | N/A | 204, 404 |

### 2.3 Terceros - Clientes y Proveedores (`/api/v1/socios`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/socios` | Autenticado | Query: `search`, `tipo`, `page`, `size` | `Page<SocioResponse>` | 200 |
| `GET` | `/api/v1/socios/{id}` | Autenticado | N/A | `SocioResponse` | 200, 404 |
| `POST` | `/api/v1/socios` | Autenticado | `GenerarSocioRequest` | `SocioResponse` | 201, 400, 409 |
| `PUT` | `/api/v1/socios/{id}` | Autenticado | `GenerarSocioRequest` | `SocioResponse` | 200, 400, 404 |

### 2.4 Catálogo de Artículos (`/api/v1/catalogo`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/catalogo/fichas` | Autenticado | Query: `search`, `habilitado`, `page`, `size` | `Page<FichaResponse>` | 200 |
| `GET` | `/api/v1/catalogo/fichas/{id}` | Autenticado | N/A | `FichaResponse` | 200, 404 |
| `GET` | `/api/v1/catalogo/fichas/ean/{ean}` | Autenticado | N/A | `FichaResponse` | 200, 404 |
| `POST` | `/api/v1/catalogo/fichas` | `ADMINISTRADOR` | `GenerarFichaRequest` | `FichaResponse` | 201, 400, 409 |
| `PATCH`| `/api/v1/catalogo/fichas/{id}/tarifa` | Autenticado | `ModificarTarifaRequest` | `FichaResponse` | 200, 400, 404 |
| `DELETE`| `/api/v1/catalogo/fichas/{id}` | `ADMINISTRADOR` | N/A | N/A | 204, 404 |

### 2.5 Inventario y Kárdex (`/api/v1/almacen`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/almacen/existencias` | Autenticado | Query: `search`, `existenciaBaja`, `page`, `size` | `Page<AlmacenNivelResponse>` | 200 |
| `GET` | `/api/v1/almacen/fichas/{id}/asientos` | `ADMINISTRADOR` | Query: `page`, `size` | `Page<AlmacenAsientoResponse>` | 200, 404 |
| `POST` | `/api/v1/almacen/apertura` | `ADMINISTRADOR` | `AlmacenAperturaRequest` | `AlmacenNivelResponse` | 201, 400, 409 |
| `POST` | `/api/v1/almacen/reajustes` | `ADMINISTRADOR` | `ReajusteAlmacenRequest` | `ReajusteAlmacen` | 201, 400, 409 |
| `GET` | `/api/v1/almacen/reajustes` | `ADMINISTRADOR` | Query: `page`, `size` | `Page<ReajusteAlmacen>` | 200 |

### 2.6 Compras (`/api/v1/abastecimientos`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/abastecimientos` | Autenticado | Query: `situacion`, `suministradorId`, `desde`, `hasta` | `Page<AbastecimientoResponse>` | 200 |
| `GET` | `/api/v1/abastecimientos/{id}` | Autenticado | N/A | `AbastecimientoResponse` | 200, 404 |
| `POST` | `/api/v1/abastecimientos` | Autenticado | `GenerarAbastecimientoRequest` | `AbastecimientoResponse` | 201, 400, 404 |
| `POST` | `/api/v1/abastecimientos/{id}/anular` | `ADMINISTRADOR` | `AnularFolioRequest` | `AbastecimientoResponse` | 200, 400, 409 |

### 2.7 Ventas de Contado (`/api/v1/expendios`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/expendios` | Autenticado | Query: `situacion`, `compradorId`, `desde`, `hasta` | `Page<ExpendioResponse>` | 200 |
| `GET` | `/api/v1/expendios/{id}` | Autenticado | N/A | `ExpendioResponse` | 200, 404 |
| `POST` | `/api/v1/expendios` | Autenticado | `GenerarExpendioRequest` | `ExpendioResponse` | 201, 400, 409 |
| `POST` | `/api/v1/expendios/{id}/anular` | `ADMINISTRADOR` | `AnularFolioRequest` | `ExpendioResponse` | 200, 400, 409 |

### 2.8 Devoluciones (`/api/v1/restituciones`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/restituciones` | Autenticado | Query: `page`, `size` | `Page<RestitucionResponse>` | 200 |
| `GET` | `/api/v1/restituciones/{id}` | Autenticado | N/A | `RestitucionResponse` | 200, 404 |
| `POST` | `/api/v1/restituciones` | Autenticado | `GenerarRestitucionRequest` | `RestitucionResponse` | 201, 400, 409 |

### 2.9 Comprobantes Imprimibles (`/api/v1/folios`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/folios/{tipo}/{correlativo}` | Autenticado | N/A | `FolioTiqueteResponse` | 200, 400, 404 |

### 2.10 Auditoría (`/api/v1/bitacora`)
| Método | Endpoint | Rol Requerido | DTO Solicitud | DTO Respuesta | Códigos HTTP |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `GET` | `/api/v1/bitacora/registros` | `ADMINISTRADOR` | Query: `modulo`, `operarioId`, `desde`, `hasta` | `Page<BitacoraLogResponse>` | 200, 403 |
