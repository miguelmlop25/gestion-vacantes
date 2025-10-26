package com.example.gestionvacantes.service;

import com.example.gestionvacantes.model.Empleador;
import com.example.gestionvacantes.repository.EmpleadorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmpleadorService {

    private final EmpleadorRepository empleadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Constructor manual
    public EmpleadorService(EmpleadorRepository empleadorRepository,
                            PasswordEncoder passwordEncoder,
                            EmailService emailService) {
        this.empleadorRepository = empleadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public Empleador registrarEmpleador(Empleador empleador) {
        if (empleadorRepository.existsByCorreo(empleador.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        empleador.setContrasenaHash(passwordEncoder.encode(empleador.getContrasenaHash()));
        empleador.setActivo(true);
        empleador.setEmailVerificado(true);

        Empleador empleadorGuardado = empleadorRepository.save(empleador);

        try {
            emailService.enviarEmailBienvenida(
                    empleador.getCorreo(),
                    empleador.getNombre(),
                    "EMPLEADOR"
            );
        } catch (Exception e) {
            System.err.println("No se pudo enviar email de bienvenida: " + e.getMessage());
        }

        System.out.println("✅ Empleador registrado y activado: " + empleador.getCorreo());
        return empleadorGuardado;
    }

    public Optional<Empleador> buscarPorCorreo(String correo) {
        return empleadorRepository.findByCorreo(correo);
    }

    public Optional<Empleador> buscarPorId(Long id) {
        return empleadorRepository.findById(id);
    }

    public Empleador actualizarEmpleador(Empleador empleador) {
        return empleadorRepository.save(empleador);
    }

    public List<Empleador> obtenerTodos() {
        return empleadorRepository.findAll();
    }
}