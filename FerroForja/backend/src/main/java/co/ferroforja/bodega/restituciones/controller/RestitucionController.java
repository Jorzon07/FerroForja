package co.ferroforja.bodega.restituciones.controller;

import co.ferroforja.bodega.restituciones.dto.request.GenerarRestitucionRequest;
import co.ferroforja.bodega.restituciones.dto.response.RestitucionResponse;
import co.ferroforja.bodega.restituciones.service.RestitucionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/restituciones")
public class RestitucionController {

    private final RestitucionService restitucionService;

    public RestitucionController(RestitucionService restitucionService) {
        this.restitucionService = restitucionService;
    }

    @GetMapping
    public ResponseEntity<Page<RestitucionResponse>> enumerarRestituciones(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(restitucionService.enumerarRestituciones(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestitucionResponse> recuperarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(restitucionService.recuperarPorId(id));
    }

    @GetMapping("/correlativo/{correlativo}")
    public ResponseEntity<RestitucionResponse> recuperarPorCorrelativo(@PathVariable String correlativo) {
        return ResponseEntity.ok(restitucionService.recuperarPorCorrelativo(correlativo));
    }

    @PostMapping
    public ResponseEntity<RestitucionResponse> asentarRestitucion(
            @Valid @RequestBody GenerarRestitucionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        RestitucionResponse creada = restitucionService.asentarRestitucion(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
