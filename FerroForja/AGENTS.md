# AGENTS.md — Sistema Web de Gestión de Inventario para Ferretería

> Instrucciones operativas para agentes de codificación (humanos e IA) que trabajen en este
> repositorio. Este archivo es la fuente de verdad sobre cómo compilar, probar, ejecutar y
> modificar el proyecto sin romper las reglas de negocio ni la arquitectura aprobada.

## 1. Mapa del repositorio

```
.
├── backend/                 # API REST — Java 21 + Spring Boot 3.3.4 + Spring Security + JPA/Hibernate
│   ├── pom.xml
│   └── src/
│       ├── main/java/co/ferroforja/bodega/
│       │   ├── BodegaApplication.java
│       │   ├── configuracion/          # OpenApiConfig, CorsConfig, AdminInitializerRunner
│       │   ├── seguridad/        # SecurityConfig (sesiones + CSRF), CustomUserDetailsService
│       │   ├── comun/          # GlobalExceptionHandler (RFC 9457), BitacoraService, HealthController
│       │   ├── acceso/            # Login/logout/sesión + GET /auth/csrf (bootstrap CSRF)
│       │   ├── operarios/            # Gestión de operarios (solo ADMIN)
│       │   ├── catalogo/         # Articulo/Fabricante/Empaque/Ficha
│       │   ├── socios/        # Socio unificado (comprador/suministrador, CC/NIT)
│       │   ├── almacen/       # Nivel atómico + Kárdex inmutable + reajustes (solo ADMIN)
│       │   ├── abastecimiento/        # Abastecimientos completos + recálculo CPP
│       │   ├── expendio/            # Expendios de contado (RN-05 comprador obligatorio, RN-06 efectivo)
│       │   ├── restituciones/         # Restituciones vinculadas (coste congelado) y libres (CPP)
│       │   ├── folios/        # Correlativos secuenciales + tiquetes imprimibles
│       │   └── reportes/       # Reservado para reportes (no generar clases ficticias)
│       └── main/resources/
│           ├── application.yml / application-{local,dev,test,prod}.yml
│           └── db/01_schema.sql, 02_initial_data.sql   # DDL/DML manuales (única vía de esquema)
│       └── test/java/co/ferroforja/bodega/        # 7 suites JUnit 5 + MockMvc
├── frontend/                # SPA — React 18 + Vite + Material UI v5 + Formik/Yup + Axios
│   └── src/
│       ├── nucleo/router/EnrutadorApp.jsx        # Rutas + ProtectedRoute por perfil
│       ├── nucleo/providers/                  # ProveedoresApp, SesionProvider (sesión), IdiomaProvider (textos ES/EN)
│       ├── servicios/api/clienteApi.js       # Axios centralizado: withCredentials + bootstrap CSRF async
│       ├── modulos/                       # acceso, tablero, catalogo, socios, almacen, abastecimientos,
│       │                                   # expendios (mostrador), restituciones, folios (impresión), bitacora, operarios, reportes
│       └── utilidades/                          # formatos.js (COP/fechas), textos.js
├── docs/                    # Documentación por etapas (1-requisitos … 7-hardening)
├── tests/load/k6-load-test.js
├── docker-compose.yml       # MySQL 8.0 + backend + frontend (Nginx)
├── .github/workflows/ci.yml
└── PROMPTS.pdf               # Metodología maestra de 7 prompts (no modificar)
```

## 2. Stack y versiones fijadas

| Capa | Tecnología | Versión / detalle |
|---|---|---|
| Backend | Java (Eclipse Temurin) | 21 LTS |
| Backend | Spring Boot | 3.3.4 (Security 6, Data JPA, Validation, Actuator) |
| Backend | Build | Maven 3.9+ (`mvn`) |
| Backend | DB real | MySQL 8.0 / InnoDB / `utf8mb4_unicode_ci` |
| Backend | DB local/test | H2 en memoria (solo `local`/`test`; jamás para validar MySQL real) |
| Frontend | Node / npm | Node 18+ (probado en v24), Vite 5, React 18 |
| Frontend | UI/forms/http | MUI v5, Formik+Yup, Axios, React Router v6 |
| Auth | Sesiones servidor | `HttpSession` + cookie `JSESSIONID` (`HttpOnly`, `SameSite=Lax`); **prohibido JWT** |
| CSRF | Cookie repository | `CookieCsrfTokenRepository.withHttpOnlyFalse()` + handler sin XOR-masking; header `X-XSRF-TOKEN` |
| Errores API | RFC 9457 | `ProblemDetail` vía `GlobalExceptionHandler` (nunca exponer stack traces) |

## 3. Comandos esenciales

```bash
# Backend — compilar + correr TODAS las pruebas (20 tests, ~15 s)
cd backend
mvn clean test

# Backend — arrancar en local (perfil 'local' = H2 en memoria + datos sintéticos)
mvn spring-boot:run
# Chequear: http://localhost:8080/api/v1/health  -> {"status":"UP"}
# Swagger:   http://localhost:8080/swagger-ui.html

# Frontend — instalar y arrancar dev (proxy /api -> :8080)
cd frontend
npm install
npm run dev
# Abrir: http://localhost:5173

# Frontend — build de producción (verifica 0 errores)
npm run build   # genera dist/
```

### Credenciales sembradas al arrancar (`AdminInitializerRunner`)

| Rol | Usuario | Contraseña | Cargo / Función |
|---|---|---|---|
| `ROLE_ADMINISTRADOR` | `gerente_forja` | `Forja#2026Master!` | Gerente General (Control Total) |
| `ROLE_ADMINISTRADOR` | `jefe_almacen` | `Bodega#2026Segura!` | Jefe de Almacén (Inventario) |
| `ROLE_USUARIO_OPERATIVO` | `cajero_mostrador` | `Caja#2026Mostrador!` | Cajera Mostrador (Punto de Venta) |
| `ROLE_ADMINISTRADOR` | `admin_forja` | `Forja#Admin26` | Soporte Técnico / Fallback |

> Se (re)crean con hash BCrypt en cada arranque si no existen —y se **reparan** si existen
> con hash inválido— para que el login siempre funcione en demo.

## 4. Reglas de negocio inviolables (RN-01 a RN-12)

1. **RN-01 Anti-stock-negativo:** toda salida usa `findByFichaIdForUpdate()` (`SELECT … FOR UPDATE`).
   Jamás verificar stock con un `SELECT` simple y restar después.
2. **RN-02 Borrado lógico:** si una referencia tiene movimientos, `habilitado=false`; nunca `DELETE` físico.
3. **RN-03 Compras completas:** recepción total en 1 transacción; prohibidas recepciones parciales.
4. **RN-04 CPP:** `nuevoCPP = (stock*cpp + vol*coste) / (stock+vol)` con `BigDecimal` escala 4,
   `HALF_UP`. Ventas congelan `costeCongeladoIndividual`. Devolución vinculada usa costo histórico;
   libre usa CPP vigente. Prohibidos `float`/`double` para dinero.
5. **RN-05:** toda venta exige `compradorId` de un tercero con `esComprador=true`.
6. **RN-06:** solo contado en efectivo; `importeAbonado >= total`. **Validar el pago ANTES de mutar stock**
   (ver `ExpendioService.asentarExpendio` en 3 pasos: pre-validar → persistir cabecera → descontar).
7. **RN-07:** devoluciones reingresan stock automáticamente; vinculada no puede exceder lo vendido.
8. **RN-08:** ajustes y cancelaciones solo `ADMINISTRADOR` (`@PreAuthorize`), motivo ≥ 5 caracteres.
9. **RN-09:** cambios de precio auditados, sin efecto retroactivo.
10. **RN-10:** un `Socio` puede ser cliente y proveedor (`esComprador`/`esSuministrador`).
11. **RN-11:** auditoría = usuario + fecha/hora + módulo/acción/descripción. Solo lectura para admin.
12. **RN-12:** consecutivos correlativos con bloqueo pesimista (`EXP-`, `CMP-`, `DEV-`, `AJU-`).
    Facturación simulada, sin DIAN real.

## 5. Patrones de implementación obligatorios

### Backend (`Controller → Service → Repository`)
- **Controller:** delgado. Solo `@Valid`, delegar al service, mapear HTTP. Nunca inyectar
  repositorios ni contener reglas de negocio. Respuestas: `200/201/204`; errores vía excepciones.
- **Service:** aquí viven las reglas + `@Transactional`. Retornar DTO/entidad, **nunca**
  `ResponseEntity`. Para mutar stock, siempre pasar por `AlmacenService`
  (`restarExistenciaConCerrojo`, `tramitarEntradaAbastecimiento`, `reponerExistencia`, …).
- **Repository:** Spring Data JPA. Para saldos y consecutivos usar `@Lock(PESSIMISTIC_WRITE)`.
- **DTO:** el cliente nunca decide precios, costos, saldos ni roles. `GenerarExpendioRequest` solo trae
  `{compradorId, importeAbonadoEfectivo, detalles:[{fichaId, volumen}]}`; el backend calcula el resto.
- **Trazabilidad Kárdex:** todo `AlmacenAsiento` debe llevar el **ID real** del documento
  (`expendioId`/`restitucionId`), nunca `0L`. Patrón: `saveAndFlush(cabecera)` → mutar stock con ese ID
  → agregar líneas → `save` final (ver `ExpendioService` y `RestitucionService`).
- **Excepciones:** `ResourceNotFoundException`→404, `ExistenciaInsuficienteException`/`BusinessException`→409,
  credenciales→401, denegación→403. Todo con `ProblemDetail` + `detail` legible.

### Seguridad web (no negociable)
- `SecurityConfig`: CORS restringido + `allowCredentials(true)`; CSRF activo con
  `CsrfCookieFilter`; `ignoringRequestMatchers` **solo** para `/health`, `/actuator/health` y
  `/auth/login`. **`/auth/csrf` NO se ignora**: debe pasar por el filtro para emitir la cookie.
- `GET /api/v1/auth/csrf` (permitAll) fuerza la emisión de `Set-Cookie: XSRF-TOKEN`.
- Frontend `clienteApi`: interceptor **asíncrono** que llama `asegurarTokenCsrf()` (GET `/auth/csrf`
  si no hay cookie) antes de cada `POST/PUT/PATCH/DELETE`, e inyecta `X-XSRF-TOKEN`.
- Contraseñas solo con `BCryptPasswordEncoder(10)`. Jamás hardcodear secretos en Git.

### Frontend
- Todo HTTP pasa por `src/services/api/clienteApi.js` (no crear instancias Axios sueltas).
- Formularios con Formik+Yup; las validaciones de negocio definitivas están en el servidor.
- Errores: mostrar `detail`/`message`/`title` del `ProblemDetail` **más el código HTTP**.
  Para 403 explicar CSRF/sesión/permiso (ver `MostradorPage`); nunca enmascarar un 403 como
  "verifique existencias".
- Formato COP/fechas solo en `utils/formatos.js`; textos en `utils/textos.js` (ES base, EN alternativo).
- Rutas admin (`/bitacora`, `/operarios`, `/observabilidad`) protegidas en `ProtectedRoute`
  **y** en el backend con `@PreAuthorize` (el frontend solo oculta; el backend autoriza).

## 6. Base de datos: procedimiento manual (sin Flyway/Liquibase)

- `backend/src/main/resources/db/01_schema.sql` = DDL (16 tablas, PK/FK/UK/CHECK/índices).
- `backend/src/main/resources/db/02_initial_data.sql` = DML de arranque.
- Perfiles: `local`/`test` → H2 `create-drop`; `dev` → `validate` contra MySQL;
  `prod` → `none` (Hibernate jamás altera el esquema).
- Aplicación manual en MySQL:
  ```bash
  mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ferroforja_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
  mysql -u root -p ferroforja_db < backend/src/main/resources/db/01_schema.sql
  mysql -u root -p ferroforja_db < backend/src/main/resources/db/02_initial_data.sql
  ```

## 7. Verificación antes de cada commit/push

```bash
cd backend && mvn clean test     # exigir: Tests run: 20, Failures: 0, Errors: 0
cd ../frontend && npm run build   # exigir: "built in" sin errores
```

- Flujo de humo manual (con backend `:8080` y frontend `:5173` arriba):
  1. Login admin → Dashboard → Catálogo (3 refs con stock) → Inventario (saldos + CPP).
  2. POS: cliente Juan Pérez + 1×Tornillo + efectivo 50000 → `EXP-XXXXXX`, cambio 35000.
  3. Compras: nueva compra → stock y CPP suben. Devolución vinculada → stock reingresa.
  4. Ajuste como operativo → debe dar **403**; como admin → **201**.
  5. Comprobante `/tiquetes/EXPENDIO/<correlativo>` imprimible.
- Si un `POST` devuelve **403 con cuerpo vacío** = falta header `X-XSRF-TOKEN` (ver §5).
  Si devuelve `detail` = regla de negocio real (stock, pago, cliente).

## 8. Prohibiciones explícitas

- No JWT, no `localStorage` de credenciales, no desactivar CSRF globalmente.
- No `ddl-auto=update` en entornos con datos; no Flyway/Liquibase sin aprobación.
- No microservicios, Kafka/Redis/K8s, CQRS/Event Sourcing, ni arquitecturas hexagonales.
- No agregar funcionalidades fuera de alcance (multisede, crédito, DIAN real, Excel, offline,
  lotes/series, devoluciones a proveedor) sin aprobación del negocio.
- No hacer `git push --force` ni `push` automático sin confirmación; no versionar secretos,
  `node_modules/`, `dist/`, `target/` ni `.env` (ver `.gitignore`).
- No inventar cifras de observabilidad: el dashboard solo consume `/health`, Actuator y RTT medido.

## 9. Convenciones

- Java: paquetes `co.ferroforja.bodega.<modulo>.{controller,dto,entity,repository,service}`;
  inyección por constructor; `BigDecimal` para dinero/cantidades; fechas `LocalDateTime` (ISO-8601).
- REST: base `/api/v1`, pluralización (`/expendios`, `/abastecimientos`, `/socios`),
  paginación `page/size/sort`, consecutivos en respuestas.
- Commits: `feat:`, `fix:`, `docs:`, `test:`, `chore:` en español, descriptivos.
- Documentos `docs/**/*.md` con metadatos (nombre, descripción, fechas, autor, versión, estado).
