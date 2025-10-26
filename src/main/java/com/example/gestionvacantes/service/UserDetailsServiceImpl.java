package com.example.gestionvacantes.service;

import com.example.gestionvacantes.model.Aspirante;
import com.example.gestionvacantes.model.Empleador;
import com.example.gestionvacantes.repository.AspiranteRepository;
import com.example.gestionvacantes.repository.EmpleadorRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de UserDetailsService para Spring Security
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AspiranteRepository aspiranteRepository;
    private final EmpleadorRepository empleadorRepository;

    // Constructor manual
    public UserDetailsServiceImpl(AspiranteRepository aspiranteRepository,
                                  EmpleadorRepository empleadorRepository) {
        this.aspiranteRepository = aspiranteRepository;
        this.empleadorRepository = empleadorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Intentar buscar como aspirante primero
        Optional<Aspirante> optAspirante = aspiranteRepository.findByCorreo(username);
        if (optAspirante.isPresent()) {
            Aspirante aspirante = optAspirante.get();
            return buildUserDetails(
                    aspirante.getCorreo(),
                    aspirante.getContrasenaHash(),
                    aspirante.getRol().name(),
                    aspirante.getActivo()
            );
        }

        // Si no es aspirante, buscar como empleador
        Optional<Empleador> optEmpleador = empleadorRepository.findByCorreo(username);
        if (optEmpleador.isPresent()) {
            Empleador empleador = optEmpleador.get();
            return buildUserDetails(
                    empleador.getCorreo(),
                    empleador.getContrasenaHash(),
                    empleador.getRol().name(),
                    empleador.getActivo()
            );
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }

    private UserDetails buildUserDetails(String username, String password, String role, boolean activo) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        return User.builder()
                .username(username)
                .password(password)
                .authorities(authorities)
                .accountLocked(!activo)
                .disabled(!activo)
                .build();
    }
}