package co.ferroforja.bodega.comun.bitacora;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bitacora")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class BitacoraController {

    private final BitacoraService bitacoraService;

    public BitacoraController(BitacoraService bitacoraService) {
        this.bitacoraService = bitacoraService;
    }

    @GetMapping("/registros")
    public ResponseEntity<Page<BitacoraLogResponse>> enumerarLogs(
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) Long operarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(bitacoraService.indagarLogs(modulo, operarioId, desde, hasta, pageable));
    }
}
