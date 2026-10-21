package co.ferroforja.bodega.folios.service;

import co.ferroforja.bodega.folios.entity.TiqueteCorrelativo;
import co.ferroforja.bodega.folios.repository.TiqueteCorrelativoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de generar consecutivos internos correlativos
 * e ininterrumpidos mediante bloqueo pesimista en base de datos (RN-12).
 */
@Service
public class FolioSecuenciaService {

    private final TiqueteCorrelativoRepository correlativoRepository;

    public FolioSecuenciaService(TiqueteCorrelativoRepository correlativoRepository) {
        this.correlativoRepository = correlativoRepository;
    }

    @Transactional
    public String recuperarSiguienteCorrelativo(String tipoIdentificacion) {
        TiqueteCorrelativo correlativo = correlativoRepository
                .findByTipoIdentificacionForUpdate(tipoIdentificacion)
                .orElseGet(() -> {
                    String prefijo = switch (tipoIdentificacion) {
                        case "EXPENDIO" -> "EXP-";
                        case "ABASTECIMIENTO" -> "CMP-";
                        case "RESTITUCION" -> "DEV-";
                        case "REAJUSTE" -> "AJU-";
                        default -> "DOC-";
                    };
                    TiqueteCorrelativo nuevo = new TiqueteCorrelativo(tipoIdentificacion, prefijo, 0L);
                    return correlativoRepository.save(nuevo);
                });

        String siguiente = correlativo.generarSiguienteCorrelativo();
        correlativoRepository.save(correlativo);
        return siguiente;
    }
}
