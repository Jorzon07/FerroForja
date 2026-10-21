package co.ferroforja.bodega.operarios.controller;

import co.ferroforja.bodega.operarios.dto.request.GenerarOperarioRequest;
import co.ferroforja.bodega.operarios.dto.response.OperarioResponse;
import co.ferroforja.bodega.operarios.service.OperarioService;
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
@RequestMapping("/api/v1/operarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class OperarioController {

    private final OperarioService operarioService;

    public OperarioController(OperarioService operarioService) {
        this.operarioService = operarioService;
    }

    @GetMapping
    public ResponseEntity<Page<OperarioResponse>> enumerarOperarios(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(operarioService.enumerarOperarios(pageable));
    }

    @PostMapping
    public ResponseEntity<OperarioResponse> generarOperario(
            @Valid @RequestBody GenerarOperarioRequest request,
            @AuthenticationPrincipal UserDetails admin) {
        OperarioResponse creado = operarioService.generarOperario(request, admin.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}/conmutar-situacion")
    public ResponseEntity<Void> conmutarSituacion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin) {
        operarioService.conmutarSituacion(id, admin.getUsername());
        return ResponseEntity.noContent().build();
    }
}
