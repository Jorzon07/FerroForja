# Documento: Plan Detallado de Implementación del Sistema

## Metadatos
* **Nombre:** 01_plan_implementacion_detallado.md
* **Descripción:** Plan estructurado de incrementos funcionales, dependencias técnicas, estrategia de pruebas y criterios de aceptación para guiar la etapa de implementación completa (Etapa 4).
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Ingeniero Senior & Líder Técnico)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Estrategia y Secuencia de Incrementos Funcionales

Para mitigar riesgos y asegurar la consistencia del inventario, la implementación de la Etapa 4 se ejecutará en **8 incrementos secuenciales**, donde cada incremento construye sobre las bases del anterior:

```
[INC-01: Seguridad & Usuarios]
         │
         ▼
[INC-02: Catálogo & Terceros]
         │
         ▼
[INC-03: Núcleo Inventario & Kárdex]
         │
         ▼
[INC-04: Almacen Apertura & Abastecimientos (CPP)]
         │
         ▼
[INC-05: Expendios de Contado (Anti-Stock Negativo)]
         │
         ▼
[INC-06: Devoluciones & Ajustes Autorizados]
         │
         ▼
[INC-07: Comprobantes & Vistas de Impresión]
         │
         ▼
[INC-08: Frontend Completo & Integración E2E]
```

---

## 2. Detalle de Incrementos

### Incremento 1: Identidad, Seguridad y Gestión de Cuentas (`INC-01`)
* **Módulos Afectados:** `auth`, `user`, `security`.
* **Objetivo:** Materializar la autenticación por sesiones web, hashing con BCrypt, inicializador del primer administrador y endpoints protegidos por RBAC.
* **Archivos Previstos:**
  * Backend: `Operario.java`, `Perfil.java`, `OperarioPerfil.java`, `OperarioRepository.java`, `AccesoService.java`, `AccesoController.java`, `AdminInitializerRunner.java`.
  * Frontend: `AccesoPage.jsx`, `SesionContext.jsx`, `ProtectedRoute.jsx`.
* **Estrategia de Pruebas:** Pruebas unitarias de codificación de contraseña y pruebas MockMvc de autenticación exitosa (200), fallida (401) y denegada (403).

### Incremento 2: Catálogo Base y Gestión de Terceros (`INC-02`)
* **Módulos Afectados:** `catalog`, `customer`, `supplier` (Terceros).
* **Objetivo:** Registrar productos, marcas, presentaciones, referencias vendibles con código de barras único y precios, junto con clientes y proveedores unificados (NIT/CC).
* **Archivos Previstos:**
  * Backend: `Articulo.java`, `Fabricante.java`, `Empaque.java`, `MedidaBase.java`, `Ficha.java`, `Socio.java`, controladores y servicios de catálogo y terceros.
  * Frontend: `ReferenceListPage.jsx`, `ThirdPartyListPage.jsx`.
* **Estrategia de Pruebas:** Pruebas unitarias de validación de código de barras único y comprobación de borrado lógico (`habilitado = false`).

### Incremento 3: Núcleo de Inventario y Kárdex Inmutable (`INC-03`)
* **Módulos Afectados:** `inventory`, `shared/audit`.
* **Objetivo:** Implementar la tabla de saldos atómicos (`almacen_nivel`) y la tabla inmutable de movimientos (`almacen_asiento`) con soporte para bloqueo pesimista contra concurrencia.
* **Archivos Previstos:**
  * Backend: `AlmacenNivel.java`, `AlmacenAsiento.java`, `InventoryRepository.java`, `AlmacenService.java`.
* **Estrategia de Pruebas:** Pruebas transaccionales que garanticen que ninguna operación modifique saldo sin generar su respectivo registro en el Kárdex.

### Incremento 4: Inventario Inicial y Módulo de Compras Completas (`INC-04`)
* **Módulos Afectados:** `inventory`, `purchase`.
* **Objetivo:** Habilitar el registro de inventario inicial para arranque de catálogo y el registro de órdenes de compra con recepción total inmediata y cálculo automático del Costo Promedio Ponderado (CPP).
* **Archivos Previstos:**
  * Backend: `Abastecimiento.java`, `AbastecimientoDetalle.java`, `PurchaseRepository.java`, `AbastecimientoService.java`, `AbastecimientoController.java`.
  * Frontend: `RecepcionNuevaPage.jsx`, `HistorialAbastecimientosPage.jsx`.
* **Estrategia de Pruebas:** Pruebas unitarias matemáticas de la fórmula de Costo Promedio Ponderado con casos de prueba numéricos exactos.

### Incremento 5: Módulo de Ventas de Contado y Cobro en Efectivo (`INC-05`)
* **Módulos Afectados:** `sale`, `inventory`.
* **Objetivo:** Registro de ventas asociadas obligatoriamente a un cliente registrado, descuento atómico de stock mediante bloqueo pesimista, registro del costo histórico de la venta y emisión de recibo.
* **Archivos Previstos:**
  * Backend: `Expendio.java`, `ExpendioDetalle.java`, `SaleRepository.java`, `ExpendioService.java`, `ExpendioController.java`.
  * Frontend: `MostradorPage.jsx`, `HistorialExpendiosPage.jsx`.
* **Estrategia de Pruebas:** Pruebas de concurrencia simulando dos hilos intentando descontar la última unidad disponible para validar que exactamente una triunfa y la otra falla limpiamente sin saldo negativo.

### Incremento 6: Devoluciones de Clientes y Ajustes de Inventario (`INC-06`)
* **Módulos Afectados:** `returns`, `inventory` (Ajustes).
* **Objetivo:** Habilitar devoluciones vinculadas a venta (con costo histórico) y no vinculadas (con CPP vigente), y ajustes manuales de stock restringidos exclusivamente al Administrador con motivo obligatorio.
* **Archivos Previstos:**
  * Backend: `Restitucion.java`, `RestitucionDetalle.java`, `ReajusteAlmacen.java`, servicios y controladores asociados.
  * Frontend: `ReturnCreatePage.jsx`, `AdjustmentDialog.jsx`.
* **Estrategia de Pruebas:** Verificar HTTP 403 ante usuario operativo en ajustes; comprobar reincorporación de cantidades devueltas al stock.

### Incremento 7: Consecutivos y Comprobantes Imprimibles (`INC-07`)
* **Módulos Afectados:** `document`.
* **Objetivo:** Manejo correlativo ininterrumpido de números de comprobante (`EXP-`, `CMP-`, `DEV-`, `AJU-`) y diseño de la vista web lista para impresión de tickets.
* **Archivos Previstos:**
  * Backend: `TiqueteCorrelativo.java`, `FolioSecuenciaService.java`, `FolioController.java`.
  * Frontend: `TiqueteImpresionPage.jsx`.
* **Estrategia de Pruebas:** Comprobación de correlativos consecutivos sin saltos.

### Incremento 8: Consolidación Frontend y Pruebas E2E (`INC-08`)
* **Módulos Afectados:** Todo el frontend React.
* **Objetivo:** Integración completa de formularios con Formik y Yup, estados de carga/error/vacío, navegación fluida y pruebas end-to-end con Playwright.
* **Archivos Previstos:** Configuración de Playwright, suites de prueba E2E de flujos de venta y compra.

---

## 3. Criterios para Avanzar Entre Bloques
No se avanzará a un nuevo incremento si el actual presenta errores de compilación, fallos en pruebas unitarias o transacciones sin atomicidad comprobada.
