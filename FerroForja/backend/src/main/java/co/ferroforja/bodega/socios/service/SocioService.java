package co.ferroforja.bodega.socios.service;

import co.ferroforja.bodega.socios.dto.request.GenerarSocioRequest;
import co.ferroforja.bodega.socios.dto.response.SocioResponse;
import co.ferroforja.bodega.socios.entity.Socio;
import co.ferroforja.bodega.socios.repository.SocioRepository;
import co.ferroforja.bodega.comun.exception.BusinessException;
import co.ferroforja.bodega.comun.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocioService {

    private final SocioRepository socioRepository;

    public SocioService(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
    }

    @Transactional(readOnly = true)
    public Page<SocioResponse> enumerarSocios(String search, String tipo, Pageable pageable) {
        boolean soloCompradores = "COMPRADOR".equalsIgnoreCase(tipo);
        boolean soloSuministradores = "SUMINISTRADOR".equalsIgnoreCase(tipo);
        return socioRepository.searchSocios(search, soloCompradores, soloSuministradores, pageable)
                .map(this::convertirARespuesta);
    }

    @Transactional(readOnly = true)
    public SocioResponse recuperarPorId(Long id) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tercero no encontrado con ID: " + id));
        return convertirARespuesta(socio);
    }

    @Transactional
    public SocioResponse generarSocio(GenerarSocioRequest request) {
        if (socioRepository.existsByNroIdentificacion(request.getNroIdentificacion())) {
            throw new BusinessException("Ya existe un tercero registrado con el documento: " + request.getNroIdentificacion());
        }

        Socio socio = new Socio();
        socio.setTipoIdentificacion(request.getTipoIdentificacion());
        socio.setNroIdentificacion(request.getNroIdentificacion().trim());
        socio.setNombreLegal(request.getNombreLegal().trim());
        socio.setTipoPersona(request.getTipoPersona());
        socio.setCelular(request.getCelular());
        socio.setEmail(request.getEmail());
        socio.setUbicacion(request.getUbicacion());
        socio.setEsComprador(request.getEsComprador());
        socio.setEsSuministrador(request.getEsSuministrador());
        socio.setHabilitado(true);

        return convertirARespuesta(socioRepository.save(socio));
    }

    @Transactional
    public SocioResponse modificarSocio(Long id, GenerarSocioRequest request) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tercero no encontrado con ID: " + id));

        if (!socio.getNroIdentificacion().equalsIgnoreCase(request.getNroIdentificacion().trim())
                && socioRepository.existsByNroIdentificacion(request.getNroIdentificacion())) {
            throw new BusinessException("Ya existe otro tercero con el documento: " + request.getNroIdentificacion());
        }

        socio.setTipoIdentificacion(request.getTipoIdentificacion());
        socio.setNroIdentificacion(request.getNroIdentificacion().trim());
        socio.setNombreLegal(request.getNombreLegal().trim());
        socio.setTipoPersona(request.getTipoPersona());
        socio.setCelular(request.getCelular());
        socio.setEmail(request.getEmail());
        socio.setUbicacion(request.getUbicacion());
        socio.setEsComprador(request.getEsComprador());
        socio.setEsSuministrador(request.getEsSuministrador());

        return convertirARespuesta(socioRepository.save(socio));
    }

    public Socio recuperarEntidadPorId(Long id) {
        return socioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tercero no encontrado con ID: " + id));
    }

    private SocioResponse convertirARespuesta(Socio t) {
        return new SocioResponse(
                t.getId(),
                t.getTipoIdentificacion(),
                t.getNroIdentificacion(),
                t.getNombreLegal(),
                t.getTipoPersona(),
                t.getCelular(),
                t.getEmail(),
                t.getUbicacion(),
                t.isEsComprador(),
                t.isEsSuministrador(),
                t.isHabilitado()
        );
    }
}
