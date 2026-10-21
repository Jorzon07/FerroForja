package co.ferroforja.bodega.almacen.controller;

import co.ferroforja.bodega.almacen.dto.request.ReajusteAlmacenRequest;
import co.ferroforja.bodega.almacen.dto.request.AlmacenAperturaRequest;
import co.ferroforja.bodega.almacen.dto.response.AlmacenAsientoResponse;
import co.ferroforja.bodega.almacen.dto.response.AlmacenNivelResponse;
import co.ferroforja.bodega.almacen.entity.ReajusteAlmacen;
import co.ferroforja.bodega.almacen.service.AlmacenReajusteService;
import co.ferroforja.bodega.almacen.service.AlmacenService;
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
@RequestMapping("/api/v1/almacen")
public class AlmacenController {

    private final AlmacenService almacenService;
    private final AlmacenReajusteService reajusteService;

    public AlmacenController(AlmacenService almacenService,
                               AlmacenReajusteService reajusteService) {
        this.almacenService = almacenService;
        this.reajusteService = reajusteService;
    }

    @GetMapping("/existencias")
    public ResponseEntity<Page<AlmacenNivelResponse>> enumerarNiveles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean existenciaBaja,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(almacenService.enumerarNiveles(search, existenciaBaja, pageable));
    }

    @GetMapping("/fichas/{id}/asientos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<AlmacenAsientoResponse>> enumerarKardex(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(almacenService.enumerarAsientosPorFicha(id, pageable));
    }

    @PostMapping("/apertura")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AlmacenNivelResponse> inicializarAlmacen(
            @Valid @RequestBody AlmacenAperturaRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        String username = admin != null ? admin.getUsername() : "SISTEMA";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(almacenService.inicializarAlmacen(request, username));
    }

    @PostMapping("/reajustes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ReajusteAlmacen> ejecutarReajuste(
            @Valid @RequestBody ReajusteAlmacenRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        String username = admin != null ? admin.getUsername() : "SISTEMA";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reajusteService.ejecutarReajuste(request, username));
    }

    @GetMapping("/reajustes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<ReajusteAlmacen>> enumerarReajustes(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reajusteService.enumerarReajustes(pageable));
    }
}
