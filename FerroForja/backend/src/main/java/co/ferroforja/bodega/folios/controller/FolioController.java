package co.ferroforja.bodega.folios.controller;

import co.ferroforja.bodega.folios.dto.response.FolioTiqueteResponse;
import co.ferroforja.bodega.folios.service.FolioService;
import co.ferroforja.bodega.comun.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/folios")
public class FolioController {

    private final FolioService folioService;

    public FolioController(FolioService folioService) {
        this.folioService = folioService;
    }

    @GetMapping("/{tipo}/{correlativo}")
    public ResponseEntity<FolioTiqueteResponse> recuperarTiquete(
            @PathVariable String tipo,
            @PathVariable String correlativo) {
        String tipoNorm = tipo.toUpperCase();
        FolioTiqueteResponse doc = switch (tipoNorm) {
            case "EXPENDIO", "EXPENDIOS" -> folioService.recuperarTiqueteExpendio(correlativo);
            case "ABASTECIMIENTO", "ABASTECIMIENTOS" -> folioService.recuperarTiqueteAbastecimiento(correlativo);
            case "RESTITUCION", "RESTITUCIONES" -> folioService.recuperarTiqueteRestitucion(correlativo);
            default -> throw new BusinessException("Tipo de documento no soportado para emisión de comprobante: " + tipo);
        };
        return ResponseEntity.ok(doc);
    }
}
