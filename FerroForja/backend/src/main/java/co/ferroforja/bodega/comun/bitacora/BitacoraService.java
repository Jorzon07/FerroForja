package co.ferroforja.bodega.comun.bitacora;

import co.ferroforja.bodega.operarios.entity.Operario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio transversal de auditoría básica (RN-11).
 * Registra usuario responsable, fecha/hora y descripción.
 */
@Service
public class BitacoraService {

    private final BitacoraLogRepository bitacoraLogRepository;

    public BitacoraService(BitacoraLogRepository bitacoraLogRepository) {
        this.bitacoraLogRepository = bitacoraLogRepository;
    }

    @Transactional
    public void asentarEvento(Operario operario, String modulo, String accion, String resena) {
        BitacoraLog log = new BitacoraLog(operario, modulo, accion, resena);
        bitacoraLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<BitacoraLogResponse> indagarLogs(String modulo, Long operarioId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        return bitacoraLogRepository.searchLogs(modulo, operarioId, desde, hasta, pageable)
                .map(log -> new BitacoraLogResponse(
                        log.getId(),
                        log.getOperario() != null ? log.getOperario().getNombreVisible() : "SISTEMA",
                        log.getMarcaTiempo(),
                        log.getModulo(),
                        log.getAccion(),
                        log.getResena()
                ));
    }
}
