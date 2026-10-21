package co.ferroforja.bodega.seguridad.service;

import co.ferroforja.bodega.operarios.entity.Operario;
import co.ferroforja.bodega.operarios.repository.OperarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Servicio de carga de usuarios para Spring Security respaldado por la tabla 'usuario'.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final OperarioRepository operarioRepository;

    public CustomUserDetailsService(OperarioRepository operarioRepository) {
        this.operarioRepository = operarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Operario operario = operarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con username: " + username));

        if (!operario.isHabilitado()) {
            throw new UsernameNotFoundException("La cuenta de usuario se encuentra inactiva");
        }

        var authorities = operario.getPerfiles().stream()
                .map(perfil -> new SimpleGrantedAuthority(perfil.getDenominacion()))
                .collect(Collectors.toSet());

        return new User(
                operario.getUsername(),
                operario.getPasswordHash(),
                authorities
        );
    }
}
