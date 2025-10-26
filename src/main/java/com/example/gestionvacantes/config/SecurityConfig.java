package com.example.gestionvacantes.config;

import com.example.gestionvacantes.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security
 * Define las reglas de autenticación y autorización
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configura el filtro de seguridad
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ----> INICIO DEL CAMBIO <----
                // Deshabilitar la protección CSRF para permitir los POST desde formularios
                .csrf(csrf -> csrf.disable())
                // ----> FIN DEL CAMBIO <----

                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas (sin autenticación)
                        .requestMatchers("/", "/index", "/auth/**", "/css/**", "/js/**", "/images/**").permitAll()

                        // Rutas solo para aspirantes
                        .requestMatchers("/aspirante/**").hasRole("ASPIRANTE")

                        // Rutas solo para empleadores
                        .requestMatchers("/empleador/**").hasRole("EMPLEADOR")

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler((request, response, authentication) -> {
                            // Redirigir según el rol
                            String rol = authentication.getAuthorities().iterator().next().getAuthority();
                            if (rol.equals("ROLE_ASPIRANTE")) {
                                response.sendRedirect("/aspirante/dashboard");
                            } else if (rol.equals("ROLE_EMPLEADOR")) {
                                response.sendRedirect("/empleador/dashboard");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/auth/acceso-denegado")
                );

        return http.build();
    }

    /**
     * Encoder de contraseñas usando BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Proveedor de autenticación
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication Manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}