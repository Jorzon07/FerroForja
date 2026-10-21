# Documento: Diagramas de Secuencia de Procesos Críticos

## Metadatos
* **Nombre:** 05_diagramas_secuencia.md
* **Descripción:** Diagramas de secuencia en formato Mermaid y explicación técnica para los flujos críticos: login, compra, venta, devolución, ajuste y cancelaciones.
* **Fecha de creación:** 13 de octubre de 2026
* **Fecha de última actualización:** 13 de octubre de 2026
* **Autor:** Agente de IA (Arquitecto de Software)
* **Versión:** 1.0
* **Estado:** Borrador (En revisión)

---

## 1. Diagrama de Secuencia: Inicio de Sesión (Login)

```mermaid
sequenceDiagram
    autonumber
    actor Usuario
    participant Frontend as React Frontend
    participant Filter as SecurityFilterChain
    participant AuthMgr as AuthenticationManager
    participant UserDetails as CustomUserDetailsService
    participant Session as HttpSession / SecurityContext

    Operario->>Frontend: Ingresa operario y contraseña
    Frontend->>Filter: POST /api/v1/auth/login {username, password}
    Filter->>AuthMgr: authenticate(UsernamePasswordAuthenticationToken)
    AuthMgr->>UserDetails: loadUserByUsername(username)
    UserDetails-->>AuthMgr: UserDetails (con BCrypt hash y perfiles)
    AuthMgr->>AuthMgr: PasswordEncoder.matches(raw, hash)
    
    alt Credenciales Válidas
        AuthMgr-->>Filter: Authentication (Authenticated)
        Filter->>Session: Almacenar SecurityContext en sesión
        Filter-->>Frontend: 200 OK + Set-Cookie: JSESSIONID; HttpOnly; SameSite=Lax
        Frontend->>Frontend: Almacenar datos de operario en SesionContext
        Frontend-->>Operario: Redirige al Dashboard
    else Credenciales Inválidas
        AuthMgr-->>Filter: BadCredentialsException
        Filter-->>Frontend: 401 Unauthorized (RFC 9457 ProblemDetail)
        Frontend-->>Operario: Muestra mensaje de error
    end
```

---

## 2. Diagrama de Secuencia: Registro de Venta de Contado (Concurrencia y Kárdex)

```mermaid
sequenceDiagram
    autonumber
    actor Operador
    participant UI as React UI (Mostrador)
    participant Ctrl as ExpendioController
    participant Svc as ExpendioService
    participant AlmSvc as AlmacenService
    participant Repo as SaleRepository / InventoryRepository
    participant DB as MySQL Database

    Operador->>UI: Selecciona comprador, agrega detalles y recibe efectivo
    UI->>Ctrl: POST /api/v1/sales {compradorId, detalles: [{fchId, vol}], efectivo}
    Ctrl->>Svc: asentarExpendio(GenerarExpendioRequest, operarioActual)
    
    Note over Svc,DB: Inicio Transacción Atómica (@Transactional)
    Svc->>AlmSvc: restarExistenciaConCerrojo(detalles)
    
    loop Por cada línea de producto
        AlmSvc->>Repo: findNivelByFichaIdForUpdate(fchId) [SELECT ... FOR UPDATE]
        Repo->>DB: Cerrojo de fila en almacen_nivel
        DB-->>Repo: Nivel actual y CPP vigente
        
        alt Nivel Libre >= Volumen Solicitada
            AlmSvc->>AlmSvc: Nuevo nivel = Nivel - Volumen
            AlmSvc->>Repo: updateNivel(nuevoNivel)
            AlmSvc->>Repo: saveAsiento(tipo: EXPENDIO, coste: CPP_vigente)
        else Stock Insuficiente (Violación RN-01)
            AlmSvc-->>Svc: Lanzar StockInsuficienteException
            Note over Svc,DB: Rollback Transaccional Completo
            Svc-->>Ctrl: Propagar Error
            Ctrl-->>UI: 409 Conflict (ProblemDetail)
            UI-->>Operador: Mostrar alerta: "Stock insuficiente"
        end
    end
    
    Svc->>Repo: saveExpendio(correlativo, detalles con coste_congelado)
    Svc->>DB: Commit Transacción
    Svc-->>Ctrl: ExpendioResponse
    Ctrl-->>UI: 201 Created (ExpendioResponse)
    UI-->>Operador: Muestra recibo listo para emitir
```

---

## 3. Diagrama de Secuencia: Registro de Compra Completa y Recálculo de Costo Promedio (CPP)

```mermaid
sequenceDiagram
    autonumber
    actor Operador
    participant UI as React UI (Abastecimientos)
    participant Ctrl as AbastecimientoController
    participant Svc as AbastecimientoService
    participant AlmSvc as AlmacenService
    participant DB as MySQL Database

    Operador->>UI: Selecciona suministrador, agrega ítems y coste individual
    UI->>Ctrl: POST /api/v1/purchases {suministradorId, detalles: [{fchId, vol, costeUnit}]}
    Ctrl->>Svc: asentarAbastecimiento(GenerarAbastecimientoRequest, operarioActual)
    
    Note over Svc,DB: Inicio Transacción Atómica (@Transactional)
    Svc->>Svc: Generar correlativo CMP-XXXXXX
    Svc->>Svc: Almacenar cabecera y detalles en situacion RECIBIDA
    
    loop Por cada línea comprada
        Svc->>AlmSvc: tramitarEntradaAbastecimiento(fchId, vol, costeUnit)
        AlmSvc->>DB: Recuperar nivel y CPP actual
        AlmSvc->>AlmSvc: Calcular nuevo CPP = [(Stock*CPP) + (Vol*CosteUnit)] / (Stock+Vol)
        AlmSvc->>AlmSvc: Nuevo stock = Stock + Vol
        AlmSvc->>DB: Modificar almacen_nivel con nuevo stock y nuevo CPP
        AlmSvc->>DB: Asentar almacen_asiento (tipo: ABASTECIMIENTO)
    end
    
    Svc->>DB: Commit Transacción
    Svc-->>Ctrl: AbastecimientoResponse
    Ctrl-->>UI: 201 Created (AbastecimientoResponse)
    UI-->>Operador: Muestra confirmación de abastecimiento y nuevo stock
```

---

## 4. Diagrama de Secuencia: Devolución de Cliente y Reincorporación de Stock

```mermaid
sequenceDiagram
    autonumber
    actor Operador
    participant UI as React UI (Restituciones)
    participant Ctrl as RestitucionController
    participant Svc as RestitucionService
    participant AlmSvc as AlmacenService
    participant DB as MySQL Database

    Operador->>UI: Ingresa correlativo de expendio, ítem, volumen devuelta y causa
    UI->>Ctrl: POST /api/v1/returns {expendioId, detalles, causa}
    Ctrl->>Svc: asentarRestitucion(GenerarRestitucionRequest, operarioActual)
    
    Note over Svc,DB: Inicio Transacción Atómica (@Transactional)
    alt Es Devolución Vinculada
        Svc->>DB: Comprobar expendio original y que suma devuelta <= expendida
        Svc->>Svc: Recuperar coste_congelado_individual de la expendio_detalle
    else Es Devolución No Vinculada
        Svc->>DB: Recuperar CPP vigente de la ficha
    end
    
    Svc->>AlmSvc: reponerExistencia(fchId, vol, costeAplicado)
    AlmSvc->>DB: Sumar volumen a almacen_nivel
    AlmSvc->>DB: Asentar almacen_asiento (tipo: RESTITUCION_COMPRADOR)
    Svc->>DB: Almacenar cabecera y detalles de restitucion (DEV-XXXXXX)
    Svc->>DB: Commit Transacción
    
    Svc-->>Ctrl: RestitucionResponse
    Ctrl-->>UI: 201 Created (RestitucionResponse)
    UI-->>Operador: Muestra tiquete de restitucion
```

---

## 5. Diagrama de Secuencia: Ajuste Manual de Inventario (Solo Administrador)

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador
    participant UI as React UI
    participant Ctrl as InventoryAdjustmentController
    participant Svc as AlmacenReajusteService
    participant DB as MySQL Database

    Admin->>UI: Selecciona ítem, tipo (INCREMENTO/DISMINUCION), volumen y causa
    UI->>Ctrl: POST /api/v1/inventory/adjustments {fchId, tipo, vol, causa}
    
    Note over Ctrl: Verificación de Perfil (@PreAuthorize("hasRole('ADMINISTRADOR')"))
    alt Usuario NO es Administrador
        Ctrl-->>UI: 403 Forbidden
        UI-->>Admin: "Acción no autorizada para su rol"
    else Usuario es Administrador
        Ctrl->>Svc: ejecutarReajuste(ReajusteRequest, adminId)
        Note over Svc,DB: Inicio Transacción
        
        alt Tipo Disminución y Nivel < Volumen
            Svc-->>Ctrl: Lanzar DiscrepanciaStockException
            Ctrl-->>UI: 409 Conflict ("Saldo resultante sería negativo")
        else Ajuste Válido
            Svc->>DB: Modificar nivel_libre
            Svc->>DB: Asentar asiento (REAJUSTE_ENTRADA o REAJUSTE_SALIDA)
            Svc->>DB: Almacenar folio de reajuste AJU-XXXXXX
            Svc->>DB: Insertar registro en bitacora_log con causa y adminId
            Svc->>DB: Commit Transacción
            Svc-->>Ctrl: ReajusteResponse
            Ctrl-->>UI: 201 Created
            UI-->>Admin: Confirma reajuste exitoso
        end
    end
```

---

## 6. Diagrama de Secuencia: Cancelación de Compra o Venta

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador
    participant UI as React UI
    participant Ctrl as ExpendioController / AbastecimientoController
    participant Svc as AnulacionService
    participant AlmSvc as AlmacenService
    participant DB as MySQL Database

    Admin->>UI: Solicita anular expendio/abastecimiento e ingresa causa
    UI->>Ctrl: POST /api/v1/{sales|purchases}/{id}/cancel {causa}
    Ctrl->>Svc: anularFolio(id, causa, adminId)
    
    Note over Svc,DB: Transacción con comprobación de consistencia
    alt Cancelación de Venta
        Svc->>DB: Chequear si la expendio tiene restituciones habilitadas
        alt Tiene Devoluciones
            Svc-->>Ctrl: 409 Conflict ("No se puede cancelar una venta con devoluciones")
        else Sin Devoluciones
            Svc->>AlmSvc: compensarEntradaStock(detallesExpendio)
            Svc->>DB: Modificar situacion de expendio a ANULADA y almacenar causa
            Svc->>DB: Asentar asientos compensatorios en Kárdex
            Svc->>DB: Auditar anulacion
            Svc-->>Ctrl: 200 OK
        end
    else Cancelación de Compra
        Svc->>DB: Chequear que stock actual >= medidas a restar de la abastecimiento
        alt Stock actual insuficiente (ya se vendió parte de la mercancía)
            Svc-->>Ctrl: 409 Conflict ("Stock ya consumido por operaciones posteriores")
        else Stock suficiente
            Svc->>AlmSvc: compensarSalidaStock(detallesAbastecimiento)
            Svc->>DB: Modificar situacion de abastecimiento a ANULADA y almacenar causa
            Svc->>DB: Asentar asientos compensatorios en Kárdex
            Svc->>DB: Auditar anulacion
            Svc-->>Ctrl: 200 OK
        end
    end
    Ctrl-->>UI: Resultado de la anulacion
```
