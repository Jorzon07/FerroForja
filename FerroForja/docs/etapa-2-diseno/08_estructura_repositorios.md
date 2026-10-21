# Documento: Estructura de Repositorios y Módulos

## Metadatos
* **Nombre:** 08_estructura_repositorios.md
* **Descripción:** Especificación técnica detallada de la organización física de paquetes del backend y directorios del frontend, con responsabilidades, clases permitidas y reglas de dependencia.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto de Software)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Principios de Organización y Separación de Repositorios
El sistema se organiza en dos proyectos desacoplados e independientes:
1. `backend/`: Proyecto Maven con Java 21 y Spring Boot 3 estructurado bajo el paquete raíz `co.ferroforja.bodega`.
2. `frontend/`: Proyecto Node/Vite con React 18+ estructurado bajo módulos funcionales (*features*).

---

## 2. Estructura Exhaustiva del Backend (`backend/`)

```
backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── emisor/
    │   │           └── almacen/
    │   │               ├── BodegaApplication.java
    │   │               ├── configuracion/
    │   │               ├── seguridad/
    │   │               │   ├── configuracion/
    │   │               │   ├── handler/
    │   │               │   └── service/
    │   │               ├── comun/
    │   │               │   ├── exception/
    │   │               │   ├── response/
    │   │               │   ├── validation/
    │   │               │   └── bitacora/
    │   │               ├── acceso/
    │   │               │   ├── controller/
    │   │               │   ├── dto/
    │   │               │   │   ├── request/
    │   │               │   │   └── response/
    │   │               │   └── service/
    │   │               ├── operarios/
    │   │               │   ├── controller/
    │   │               │   ├── dto/{request, response}/
    │   │               │   ├── entity/
    │   │               │   ├── repository/
    │   │               │   └── service/
    │   │               ├── catalogo/
    │   │               │   ├── controller/
    │   │               │   ├── dto/{request, response}/
    │   │               │   ├── entity/
    │   │               │   ├── repository/
    │   │               │   └── service/
    │   │               ├── socios/
    │   │               │   ├── controller/
    │   │               │   ├── dto/{request, response}/
    │   │               │   ├── entity/
    │   │               │   ├── repository/
    │   │               │   └── service/
    │   │               ├── suministro/
    │   │               │   ├── controller/
    │   │               │   ├── dto/{request, response}/
    │   │               │   ├── entity/
    │   │               │   ├── repository/
    │   │               │   └── service/
    │   │               ├── almacen/
    │   │               │   ├── controller/
    │   │               │   ├── dto/{request, response}/
    │   │               │   ├── entity/
    │   │               │   ├── repository/
    │   │               │   └── service/
    │   │               ├── abastecimiento/
    │   │               │   ├── controller/
    │   │               │   ├── dto/{request, response}/
    │   │               │   ├── entity/
    │   │               │   ├── repository/
    │   │               │   └── service/
    │   │               ├── expendio/
    │   │               │   ├── controller/
    │   │               │   ├── dto/{request, response}/
    │   │               │   ├── entity/
    │   │               │   ├── repository/
    │   │               │   └── service/
    │   │               ├── restituciones/
    │   │               │   ├── controller/
    │   │               │   ├── dto/{request, response}/
    │   │               │   ├── entity/
    │   │               │   ├── repository/
    │   │               │   └── service/
    │   │               ├── folios/
    │   │               │   ├── controller/
    │   │               │   ├── dto/{request, response}/
    │   │               │   ├── entity/
    │   │               │   ├── repository/
    │   │               │   └── service/
    │   │               └── reportes/
    │   │                   ├── controller/
    │   │                   ├── dto/
    │   │                   ├── repository/
    │   │                   └── service/
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-test.yml
    │       └── application-prod.yml
    └── test/
        └── java/
            └── com/
                └── emisor/
                    └── almacen/
                        ├── acceso/
                        ├── catalogo/
                        ├── almacen/
                        ├── abastecimiento/
                        ├── expendio/
                        └── ...
```

### 2.1 Responsabilidades y Reglas de Dependencia por Paquete Backend

* **`config/`:** Configuración transversal de Spring (CORS, Swagger/OpenAPI, Beans de serialización Jackson).
  * *Prohibido:* Definir lógica de negocio o entidades aquí.
* **`security/`:**
  * `config/`: Clase `SecurityConfig` definiendo el bean `SecurityFilterChain`.
  * `handler/`: Manejadores de autenticación exitosa, fallida o accesos denegados (`CustomAccessDeniedHandler`, `CustomAuthenticationEntryPoint`).
  * `service/`: Implementación de `UserDetailsService`.
* **`shared/`:**
  * `exception/`: `@RestControllerAdvice`, excepciones base del dominio (`ResourceNotFoundException`, `ExistenciaInsuficienteException`, `BusinessRuleException`).
  * `audit/`: Entidad `BitacoraLog`, repositorio y servicio transversal para registro de bitácora.
* **Paquetes Funcionales (`catalog`, `inventory`, `purchase`, `sale`, `returns`, etc.):**
  * `controller/`: Solo inyecta el `Service` de su módulo. Retorna DTOs o códigos HTTP adecuados. **Nunca inyecta Repositories ni entidades directamente**.
  * `service/`: Contiene la lógica del caso de uso, validación de reglas de negocio y transaccionalidad (`@Transactional`). Inyecta repositorios propios y, si es necesario, servicios de otros módulos (ej. `ExpendioService` invoca `AlmacenService`).
  * `repository/`: Interfaces que extienden `JpaRepository`.
  * `entity/`: Entidades anotadas con `@Entity`, `@Table`.
  * `dto/`: Contratos formales de entrada (`request`) y salida (`response`). Ningún DTO debe exponer la entidad JPA entera.

---

## 3. Estructura Exhaustiva del Frontend (`frontend/`)

```
frontend/
├── package.json
├── index.html
├── vite.config.js
├── public/
│   └── favicon.ico
└── src/
    ├── main.jsx
    ├── App.jsx
    ├── nucleo/
    │   ├── enrutador/          # EnrutadorApp.jsx, ProtectedRoute.jsx
    │   ├── suministradores/       # ProveedoresApp.jsx, SesionProvider.jsx
    │   └── theme/           # theme.js (paleta MUI, fuentes)
    ├── assets/              # imágenes, logos
    ├── componentes/
    │   ├── maquetacion/          # MarcoPrincipal.jsx, Header.jsx, Sidebar.jsx
    │   ├── feedback/        # LoadingBackdrop.jsx, NotificationSnackbar.jsx
    │   ├── forms/           # FormikInput.jsx, FormikSelect.jsx, CurrencyInput.jsx
    │   ├── tables/          # AppTable.jsx, AppPagination.jsx
    │   └── common/          # PageTitle.jsx, ConfirmModal.jsx, StatusChip.jsx
    ├── modulos/
    │   ├── acceso/            # AccesoPage.jsx, accesoService.js
    │   ├── operarios/           # UserListPage.jsx, operarioService.js
    │   ├── catalogo/         # CatalogListPage.jsx, catalogoService.js
    │   ├── socios/       # CustomerListPage.jsx, customerService.js
    │   ├── suppliers/       # SupplierListPage.jsx, supplierService.js
    │   ├── almacen/       # InventoryOverviewPage.jsx, KardexModal.jsx, almacenService.js
    │   ├── abastecimientos/       # PurchaseNewPage.jsx, HistorialAbastecimientosPage.jsx, abastecimientoService.js
    │   ├── expendios/           # POSPage.jsx, HistorialExpendiosPage.jsx, expendioService.js
    │   ├── restituciones/         # ReturnNewPage.jsx, restitucionService.js
    │   ├── folios/       # PrintVoucherPage.jsx, folioService.js
    │   └── reportes/         # ValuationReportPage.jsx, reportService.js
    ├── servicios/
    │   └── api/             # clienteApi.js, endpoints.js
    ├── hooks/               # useSesion.js, useNotification.js
    ├── utilidades/               # formatos.js, constants.js
    └── tests/               # tests unitarios y configuración de Playwright
```

### 3.1 Responsabilidades de Capas en Frontend
* **`app/`:** Inicialización global de la aplicación (enrutamiento de rutas protegidas según sesión y roles, proveedores de contexto y tema visual de Material UI).
* **`components/`:** Componentes reutilizables agnósticos del dominio del negocio (layouts, inputs genéricos, tablas base con paginación).
* **`features/`:** Organización modular vertical. Cada carpeta contiene las vistas (`pages`), modales/formularios propios (`components`), llamadas a la API (`services`) y esquemas Yup (`validation`).
* **`services/api/`:** Cliente Axios centralizado (`clienteApi.js`) con manejo de cookies, interceptores para tokens CSRF y captura global de respuestas 401/403.
