package co.ferroforja.bodega.expendio.controller;

import co.ferroforja.bodega.abastecimiento.dto.request.AnularFolioRequest;
import co.ferroforja.bodega.expendio.dto.request.GenerarExpendioRequest;
import co.ferroforja.bodega.expendio.dto.response.ExpendioResponse;
import co.ferroforja.bodega.expendio.service.ExpendioService;
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
@RequestMapping("/api/v1/expendios")
public class ExpendioController {

    private final ExpendioService expendioService;

    public ExpendioController(ExpendioService expendioService) {
        this.expendioService = expendioService;
    }

    @GetMapping
    public ResponseEntity<Page<ExpendioResponse>> enumerarExpendios(
            @RequestParam(required = false) String situacion,
            @RequestParam(required = false) Long compradorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(expendioService.enumerarExpendios(situacion, compradorId, desde, hasta, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpendioResponse> recuperarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(expendioService.recuperarPorId(id));
    }

    @GetMapping("/correlativo/{correlativo}")
    public ResponseEntity<ExpendioResponse> recuperarPorCorrelativo(@PathVariable String correlativo) {
        return ResponseEntity.ok(expendioService.recuperarPorCorrelativo(correlativo));
    }

    @PostMapping
    public ResponseEntity<ExpendioResponse> asentarExpendio(
            @Valid @RequestBody GenerarExpendioRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        ExpendioResponse creada = expendioService.asentarExpendio(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ExpendioResponse> anularExpendio(
            @PathVariable Long id,
            @Valid @RequestBody AnularFolioRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        String username = admin != null ? admin.getUsername() : "SISTEMA";
        return ResponseEntity.ok(expendioService.anularExpendio(id, request, username));
    }
}
