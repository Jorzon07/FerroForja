package co.ferroforja.bodega.catalogo.controller;

import co.ferroforja.bodega.catalogo.dto.request.ModificarTarifaRequest;
import co.ferroforja.bodega.catalogo.dto.request.GenerarFichaRequest;
import co.ferroforja.bodega.catalogo.dto.response.FichaResponse;
import co.ferroforja.bodega.catalogo.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/catalogo")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/fichas")
    public ResponseEntity<Page<FichaResponse>> enumerarFichas(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean habilitado,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(catalogoService.enumerarFichas(search, habilitado, pageable));
    }

    @GetMapping("/fichas/{id}")
    public ResponseEntity<FichaResponse> recuperarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.recuperarPorId(id));
    }

    @GetMapping("/fichas/ean/{ean}")
    public ResponseEntity<FichaResponse> recuperarPorCodigoEan(@PathVariable String barcode) {
        return ResponseEntity.ok(catalogoService.recuperarPorCodigoEan(barcode));
    }

    @GetMapping("/bases")
    public ResponseEntity<java.util.Map<String, Object>> recuperarBases() {
        return ResponseEntity.ok(catalogoService.recuperarBases());
    }

    @PostMapping("/fichas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<FichaResponse> generarFicha(
            @Valid @RequestBody GenerarFichaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        FichaResponse creada = catalogoService.generarFicha(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PatchMapping("/fichas/{id}/conmutar-situacion")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<FichaResponse> conmutarSituacion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        return ResponseEntity.ok(catalogoService.conmutarSituacion(id, username));
    }

    @PatchMapping("/fichas/{id}/tarifa")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO_OPERATIVO')")
    public ResponseEntity<FichaResponse> modificarTarifa(
            @PathVariable Long id,
            @Valid @RequestBody ModificarTarifaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        return ResponseEntity.ok(catalogoService.modificarTarifa(id, request, username));
    }

    @DeleteMapping("/fichas/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deshabilitarFicha(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        catalogoService.deshabilitarFicha(id, username);
        return ResponseEntity.noContent().build();
    }
}
