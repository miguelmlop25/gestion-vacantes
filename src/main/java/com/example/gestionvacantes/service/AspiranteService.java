package com.example.gestionvacantes.service;

import com.example.gestionvacantes.model.Aspirante;
import com.example.gestionvacantes.repository.AspiranteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AspiranteService {

    private final AspiranteRepository aspiranteRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Constructor manual
    public AspiranteService(AspiranteRepository aspiranteRepository,
                            PasswordEncoder passwordEncoder,
                            EmailService emailService) {
        this.aspiranteRepository = aspiranteRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public Aspirante registrarAspirante(Aspirante aspirante) {
        if (aspiranteRepository.existsByCorreo(aspirante.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        aspirante.setContrasenaHash(passwordEncoder.encode(aspirante.getContrasenaHash()));
        aspirante.setActivo(true);
        aspirante.setEmailVerificado(true);

        Aspirante aspiranteGuardado = aspiranteRepository.save(aspirante);

        try {
            emailService.enviarEmailBienvenida(
                    aspirante.getCorreo(),
                    aspirante.getNombre(),
                    "ASPIRANTE"
            );
        } catch (Exception e) {
            System.err.println("No se pudo enviar email de bienvenida: " + e.getMessage());
        }

        System.out.println("✅ Aspirante registrado y activado: " + aspirante.getCorreo());
        return aspiranteGuardado;
    }

    public Optional<Aspirante> buscarPorCorreo(String correo) {
        return aspiranteRepository.findByCorreo(correo);
    }

    public Optional<Aspirante> buscarPorId(Long id) {
        return aspiranteRepository.findById(id);
    }

    public Aspirante actualizarAspirante(Aspirante aspirante) {
        return aspiranteRepository.save(aspirante);
    }

    public List<Aspirante> obtenerTodos() {
        return aspiranteRepository.findAll();
    }

    // ... (dentro de la clase AspiranteService)

    @Transactional
    public void actualizarHabilidades(Long aspiranteId, String habilidades) {
        Aspirante aspirante = aspiranteRepository.findById(aspiranteId)
                .orElseThrow(() -> new RuntimeException("Aspirante no encontrado"));
        aspirante.setHabilidades(habilidades);
        aspiranteRepository.save(aspirante);
    }
}