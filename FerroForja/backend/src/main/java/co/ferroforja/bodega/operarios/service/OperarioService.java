package co.ferroforja.bodega.operarios.service;

import co.ferroforja.bodega.comun.bitacora.BitacoraService;
import co.ferroforja.bodega.comun.exception.BusinessException;
import co.ferroforja.bodega.comun.exception.ResourceNotFoundException;
import co.ferroforja.bodega.operarios.dto.request.GenerarOperarioRequest;
import co.ferroforja.bodega.operarios.dto.response.OperarioResponse;
import co.ferroforja.bodega.operarios.entity.Perfil;
import co.ferroforja.bodega.operarios.entity.Operario;
import co.ferroforja.bodega.operarios.repository.PerfilRepository;
import co.ferroforja.bodega.operarios.repository.OperarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OperarioService {

    private final OperarioRepository operarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final BitacoraService bitacoraService;

    public OperarioService(OperarioRepository operarioRepository, PerfilRepository perfilRepository,
                       PasswordEncoder passwordEncoder, BitacoraService bitacoraService) {
        this.operarioRepository = operarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
        this.bitacoraService = bitacoraService;
    }

    @Transactional(readOnly = true)
    public Page<OperarioResponse> enumerarOperarios(Pageable pageable) {
        return operarioRepository.findAll(pageable).map(this::convertirARespuesta);
    }

    @Transactional
    public OperarioResponse generarOperario(GenerarOperarioRequest request, String adminUsername) {
        if (operarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El nombre de usuario ya está registrado");
        }

        Operario operario = new Operario();
        operario.setUsername(request.getUsername());
        operario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        operario.setNombreVisible(request.getNombreVisible());
        operario.setEmail(request.getEmail());
        operario.setHabilitado(true);

        Set<Perfil> perfiles = new HashSet<>();
        for (String perfilDenominacion : request.getPerfiles()) {
            Perfil perfil = perfilRepository.findByDenominacion(perfilDenominacion)
                    .orElseGet(() -> perfilRepository.save(new Perfil(null, perfilDenominacion, "Rol " + perfilDenominacion)));
            perfiles.add(perfil);
        }
        operario.setPerfiles(perfiles);

        Operario guardado = operarioRepository.save(operario);

        Operario admin = operarioRepository.findByUsername(adminUsername).orElse(null);
        bitacoraService.asentarEvento(admin, "OPERARIOS", "GENERAR_OPERARIO", "Creación de usuario " + guardado.getUsername());

        return convertirARespuesta(guardado);
    }

    @Transactional
    public void conmutarSituacion(Long id, String adminUsername) {
        Operario operario = operarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        operario.setHabilitado(!operario.isHabilitado());
        operarioRepository.save(operario);

        Operario admin = operarioRepository.findByUsername(adminUsername).orElse(null);
        bitacoraService.asentarEvento(admin, "OPERARIOS", "SITUACION_OPERARIO",
                "Cambio de estado a " + (operario.isHabilitado() ? "HABILITADO" : "DESHABILITADO") + " para usuario " + operario.getUsername());
    }

    private OperarioResponse convertirARespuesta(Operario operario) {
        Set<String> perfiles = operario.getPerfiles().stream()
                .map(Perfil::getDenominacion)
                .collect(Collectors.toSet());
        return new OperarioResponse(
                operario.getId(),
                operario.getUsername(),
                operario.getNombreVisible(),
                operario.getEmail(),
                operario.isHabilitado(),
                perfiles,
                operario.getCreadoEn()
        );
    }
}
