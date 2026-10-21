# Documento: Matriz de Trazabilidad del Diseño Técnico

## Metadatos
* **Nombre:** 10_matriz_trazabilidad_diseno.md
* **Descripción:** Matriz integral de trazabilidad que conecta los requisitos aprobados de la Etapa 1 con las entidades, atributos, servicios, endpoints REST, pantallas y diagramas de diseño definidos en la Etapa 2.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto de Software y Requisitos)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Matriz de Trazabilidad Integral: Requisitos a Artefactos de Diseño

| ID Requisito | Entidad JPA y Tablas | Atributos Críticos | Servicio Backend | Endpoint REST | Pantalla / Componente Frontend | Diagrama de Secuencia | Criterio de Aceptación |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **RF-CAT-001** | `Ficha`, `Articulo`, `Fabricante`, `Empaque` | `codigo_ean (UK)`, `tarifa_publico`, `habilitado` | `CatalogoService` | `POST /api/v1/catalog/references` | `ReferenceListPage`, `ReferenceFormDialog` | N/A (CRUD Estándar) | **CA-SC-006** |
| **RF-CAT-002** | `Ficha` | `habilitado (boolean)` | `CatalogoService` | `DELETE /api/v1/catalog/references/{id}` | `ReferenceListPage` (Botón Desactivar) | N/A (Soft-Delete) | **CA-SC-006** |
| **RF-CAT-003** | `Ficha`, `BitacoraLog` | `tarifa_publico`, `bitacora_log.resena` | `CatalogoService` | `PATCH /api/v1/catalog/references/{id}/price` | `PriceEditDialog` | N/A (Auditoría) | **CA-SC-009** |
| **RF-INV-001** | `AlmacenNivel` | `volumen_libre`, `coste_medio_ponderado` | `AlmacenService` | `GET /api/v1/inventory/stocks` | `StockOverviewPage` | N/A (Lectura) | **CA-SC-001** |
| **RF-INV-002** | `AlmacenAsiento` | `tipo_asiento`, `volumen`, `coste_individual_asiento` | `AlmacenService` | `GET /api/v1/inventory/references/{id}/movements` | `KardexModal` | Diagramas 2, 3, 4 | **CA-SC-002** |
| **RF-INV-003** | `AlmacenNivel`, `AlmacenAsiento` | `volumen_libre`, `coste_medio_ponderado` | `AlmacenService` | `POST /api/v1/inventory/initial` | `InitialStockDialog` | N/A (Admin) | **CA-SC-005** |
| **RF-INV-004** | `AlmacenNivel` | `volumen_libre >= 0 (CHECK)` | `AlmacenService` | `POST /api/v1/sales` (Validación) | `MostradorPage` | **Diagrama 2: Venta** | **CA-SC-001** |
| **RF-COM-001** | `Abastecimiento`, `AbastecimientoDetalle` | `situacion = 'RECIBIDA'`, `correlativo`, `total` | `AbastecimientoService` | `POST /api/v1/purchases` | `RecepcionNuevaPage` | **Diagrama 3: Compra** | **CA-SC-004** |
| **RF-COM-002** | `AlmacenNivel` | `coste_medio_ponderado` | `AlmacenService` | Invocado por `AbastecimientoService` | `HistorialAbastecimientosPage` | **Diagrama 3: Compra** | **CA-SC-005** |
| **RF-COM-003** | `Abastecimiento`, `AlmacenAsiento` | `situacion = 'ANULADA'`, `causa_anulacion` | `AbastecimientoService` | `POST /api/v1/purchases/{id}/cancel` | `HistorialAbastecimientosPage` | **Diagrama 6: Cancelación** | **CA-SC-004** |
| **RF-VEN-001** | `Expendio`, `ExpendioDetalle` | `correlativo`, `coste_congelado_individual`, `total` | `ExpendioService` | `POST /api/v1/sales` | `MostradorPage` | **Diagrama 2: Venta** | **CA-SC-002** |
| **RF-VEN-002** | `Expendio`, `AlmacenAsiento` | `situacion = 'ANULADA'`, `causa_anulacion` | `ExpendioService` | `POST /api/v1/sales/{id}/cancel` | `HistorialExpendiosPage` | **Diagrama 6: Cancelación** | **CA-SC-008** |
| **RF-DEV-001** | `Restitucion`, `RestitucionDetalle` | `tipo = 'VINCULADA'`, `coste_individual_aplicado` | `RestitucionService` | `POST /api/v1/returns` | `ReturnCreatePage` | **Diagrama 4: Devolución** | **CA-SC-007** |
| **RF-DEV-002** | `Restitucion`, `RestitucionDetalle` | `tipo = 'NO_VINCULADA'`, `coste_individual_aplicado` | `RestitucionService` | `POST /api/v1/returns` | `ReturnCreatePage` | **Diagrama 4: Devolución** | **CA-SC-007** |
| **RF-AJU-001** | `ReajusteAlmacen`, `BitacoraLog` | `tipo_reajuste`, `volumen`, `causa` | `AlmacenReajusteService` | `POST /api/v1/inventory/adjustments` | `AdjustmentDialog` | **Diagrama 5: Ajuste** | **CA-SC-008** |
| **RF-TER-001** | `Socio` | `nro_identificacion (UK)`, `es_comprador`, `es_suministrador` | `SocioService` | `POST /api/v1/third-parties` | `ThirdPartyListPage`, `ThirdPartyDialog` | N/A | **CA-SC-002** |
| **RF-DOC-001** | `TiqueteCorrelativo` | `tipo_identificacion (UK)`, `ultimo_nro`, `prefijo` | `FolioSecuenciaService` | Invocado internamente | Mostrado en cabeceras de vistas | N/A | **CA-SC-010** |
| **RF-DOC-002** | N/A (Presentación DTO) | `FolioTiqueteResponse` | `FolioService` | `GET /api/v1/documents/{tipo}/{id}` | `TiqueteImpresionPage` | N/A | **CA-SC-010** |
| **RF-SEG-001** | `Operario`, `Perfil` | `username (UK)`, `password_hash (BCrypt)` | `AccesoService` | `POST /api/v1/auth/login` | `AccesoPage` | **Diagrama 1: Login** | **CA-SC-003** |
| **RF-SEG-002** | `OperarioPerfil`, `Perfil` | `denominacion = ROLE_ADMINISTRADOR / ROLE_USUARIO_OPERATIVO` | `SecurityFilterChain` | Todos los endpoints (Filtros RBAC) | Navegación condicionada por rol | Diagramas 1 y 5 | **CA-SC-003** |
| **RF-SEG-003** | `BitacoraLog` | `operario_id`, `marca_tiempo`, `modulo`, `accion`, `resena` | `BitacoraService` | `GET /api/v1/audit/logs` | `AuditLogPage` | N/A | **CA-SC-009** |

---

## 2. Cobertura del Diseño
* Todos los requisitos de la Etapa 1 tienen asignación directa a entidades, servicios, controladores, rutas frontend y criterios de verificación.
* Las reglas de negocio críticas (`RN-01`, `RN-02`, `RN-03`, `RN-04`, `RN-08`) se materializan en restricciones a nivel de base de datos (`CHECK`, `UK`, `RESTRICT`) y en validaciones transaccionales en la capa `@Service`.
