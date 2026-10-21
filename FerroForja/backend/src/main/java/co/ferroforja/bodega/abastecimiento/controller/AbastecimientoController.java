package co.ferroforja.bodega.abastecimiento.controller;

import co.ferroforja.bodega.abastecimiento.dto.request.AnularFolioRequest;
import co.ferroforja.bodega.abastecimiento.dto.request.GenerarAbastecimientoRequest;
import co.ferroforja.bodega.abastecimiento.dto.response.AbastecimientoResponse;
import co.ferroforja.bodega.abastecimiento.service.AbastecimientoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/abastecimientos")
public class AbastecimientoController {

    private final AbastecimientoService abastecimientoService;

    public AbastecimientoController(AbastecimientoService abastecimientoService) {
        this.abastecimientoService = abastecimientoService;
    }

    @GetMapping
    public ResponseEntity<Page<AbastecimientoResponse>> enumerarAbastecimientos(
            @RequestParam(required = false) String situacion,
            @RequestParam(required = false) Long suministradorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(abastecimientoService.enumerarAbastecimientos(situacion, suministradorId, desde, hasta, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbastecimientoResponse> recuperarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(abastecimientoService.recuperarPorId(id));
    }

    @GetMapping("/correlativo/{correlativo}")
    public ResponseEntity<AbastecimientoResponse> recuperarPorCorrelativo(@PathVariable String correlativo) {
        return ResponseEntity.ok(abastecimientoService.recuperarPorCorrelativo(correlativo));
    }

    @PostMapping
    public ResponseEntity<AbastecimientoResponse> asentarAbastecimiento(
            @Valid @RequestBody GenerarAbastecimientoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        AbastecimientoResponse creada = abastecimientoService.asentarAbastecimiento(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AbastecimientoResponse> anularAbastecimiento(
            @PathVariable Long id,
            @Valid @RequestBody AnularFolioRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        String username = admin != null ? admin.getUsername() : "SISTEMA";
        return ResponseEntity.ok(abastecimientoService.anularAbastecimiento(id, request, username));
    }
}
