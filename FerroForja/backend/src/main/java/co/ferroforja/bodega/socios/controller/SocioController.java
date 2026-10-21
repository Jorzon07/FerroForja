package co.ferroforja.bodega.socios.controller;

import co.ferroforja.bodega.socios.dto.request.GenerarSocioRequest;
import co.ferroforja.bodega.socios.dto.response.SocioResponse;
import co.ferroforja.bodega.socios.service.SocioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/socios")
public class SocioController {

    private final SocioService socioService;

    public SocioController(SocioService socioService) {
        this.socioService = socioService;
    }

    @GetMapping
    public ResponseEntity<Page<SocioResponse>> enumerarSocios(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "TODOS") String tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(socioService.enumerarSocios(search, tipo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SocioResponse> recuperarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(socioService.recuperarPorId(id));
    }

    @PostMapping
    public ResponseEntity<SocioResponse> generarSocio(@Valid @RequestBody GenerarSocioRequest request) {
        SocioResponse creado = socioService.generarSocio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SocioResponse> modificarSocio(
            @PathVariable Long id,
            @Valid @RequestBody GenerarSocioRequest request) {
        return ResponseEntity.ok(socioService.modificarSocio(id, request));
    }
}
