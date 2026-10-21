package co.ferroforja.bodega.acceso.service;

import co.ferroforja.bodega.acceso.dto.request.LoginRequest;
import co.ferroforja.bodega.acceso.dto.response.AccesoResponse;
import co.ferroforja.bodega.comun.bitacora.BitacoraService;
import co.ferroforja.bodega.comun.exception.ResourceNotFoundException;
import co.ferroforja.bodega.operarios.entity.Perfil;
import co.ferroforja.bodega.operarios.entity.Operario;
import co.ferroforja.bodega.operarios.repository.OperarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccesoService {

    private final OperarioRepository operarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final BitacoraService bitacoraService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AccesoService(OperarioRepository operarioRepository, PasswordEncoder passwordEncoder, BitacoraService bitacoraService) {
        this.operarioRepository = operarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.bitacoraService = bitacoraService;
    }

    @Transactional
    public AccesoResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Operario operario = operarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!operario.isHabilitado()) {
            throw new BadCredentialsException("Cuenta de usuario inactiva");
        }

        if (!passwordEncoder.matches(request.getPassword(), operario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        List<String> perfiles = operario.getPerfiles().stream()
                .map(Perfil::getDenominacion)
                .toList();

        var authorities = operario.getPerfiles().stream()
                .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.getDenominacion()))
                .toList();

        org.springframework.security.core.userdetails.UserDetails principal =
                new org.springframework.security.core.userdetails.User(
                        operario.getUsername(),
                        operario.getPasswordHash(),
                        authorities
                );

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        bitacoraService.asentarEvento(operario, "SEGURIDAD", "INICIO_SESION", "Inicio de sesión exitoso del usuario " + operario.getUsername());

        return new AccesoResponse(operario.getId(), operario.getUsername(), operario.getNombreVisible(), operario.getEmail(), perfiles);
    }

    @Transactional(readOnly = true)
    public AccesoResponse getOperarioHabilitado(String username) {
        Operario operario = operarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<String> perfiles = operario.getPerfiles().stream()
                .map(Perfil::getDenominacion)
                .toList();

        return new AccesoResponse(operario.getId(), operario.getUsername(), operario.getNombreVisible(), operario.getEmail(), perfiles);
    }

    public void logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}
