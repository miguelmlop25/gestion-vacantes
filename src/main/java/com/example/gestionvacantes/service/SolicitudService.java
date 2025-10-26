package com.example.gestionvacantes.service;

import com.example.gestionvacantes.model.Aspirante;
import com.example.gestionvacantes.model.Solicitud;
import com.example.gestionvacantes.model.Vacante;
import com.example.gestionvacantes.model.enums.EstadoSolicitud;
import com.example.gestionvacantes.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    // Constructor manual
    public SolicitudService(SolicitudRepository solicitudRepository,
                            FileStorageService fileStorageService,
                            EmailService emailService) {
        this.solicitudRepository = solicitudRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
    }

    public Solicitud crearSolicitud(Aspirante aspirante, Vacante vacante, MultipartFile cvFile) {
        if (solicitudRepository.existsByAspiranteIdAndVacanteId(aspirante.getId(), vacante.getId())) {
            throw new RuntimeException("Ya has aplicado a esta vacante");
        }

        if (!vacante.isActiva()) {
            throw new RuntimeException("Esta vacante ya no está disponible");
        }

        String nombreArchivo = fileStorageService.almacenarArchivo(cvFile);

        Solicitud solicitud = new Solicitud();
        solicitud.setAspirante(aspirante);
        solicitud.setVacante(vacante);
        solicitud.setCvAdjunto(nombreArchivo);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setActiva(true);

        Solicitud solicitudGuardada = solicitudRepository.save(solicitud);

        emailService.enviarNotificacionNuevaSolicitud(solicitudGuardada);

        System.out.println("✅ Solicitud creada: Aspirante " + aspirante.getId() +
                " -> Vacante " + vacante.getId());

        return solicitudGuardada;
    }

    public List<Solicitud> obtenerSolicitudesPorAspirante(Long aspiranteId) {
        return solicitudRepository.findByAspiranteIdOrderByFechaSolicitudDesc(aspiranteId);
    }

    public List<Solicitud> obtenerSolicitudesPorVacante(Long vacanteId) {
        return solicitudRepository.findByVacanteIdOrderByFechaSolicitudDesc(vacanteId);
    }

    public List<Solicitud> obtenerSolicitudesPorEmpleador(Long empleadorId) {
        return solicitudRepository.findByEmpleadorId(empleadorId);
    }

    public Optional<Solicitud> buscarPorId(Long id) {
        return solicitudRepository.findById(id);
    }

    public Solicitud actualizarEstado(Long solicitudId, EstadoSolicitud nuevoEstado, String nota) {
        Optional<Solicitud> optSolicitud = solicitudRepository.findById(solicitudId);

        if (optSolicitud.isEmpty()) {
            throw new RuntimeException("Solicitud no encontrada");
        }

        Solicitud solicitud = optSolicitud.get();
        solicitud.setEstado(nuevoEstado);
        if (nota != null && !nota.trim().isEmpty()) {
            solicitud.setNotaReclutador(nota);
        }

        return solicitudRepository.save(solicitud);
    }

    public Solicitud programarEntrevista(Long solicitudId, LocalDateTime fechaEntrevista, String detalles) {
        Optional<Solicitud> optSolicitud = solicitudRepository.findById(solicitudId);

        if (optSolicitud.isEmpty()) {
            throw new RuntimeException("Solicitud no encontrada");
        }

        Solicitud solicitud = optSolicitud.get();
        solicitud.programarEntrevista(fechaEntrevista, detalles);

        Solicitud solicitudGuardada = solicitudRepository.save(solicitud);

        emailService.enviarNotificacionEntrevista(solicitudGuardada);

        solicitudGuardada.setNotificacionEnviada(true);
        solicitudRepository.save(solicitudGuardada);

        System.out.println("✅ Entrevista programada para solicitud ID: " + solicitudId);

        return solicitudGuardada;
    }

    public Solicitud rechazarSolicitud(Long solicitudId, String motivo) {
        Optional<Solicitud> optSolicitud = solicitudRepository.findById(solicitudId);

        if (optSolicitud.isEmpty()) {
            throw new RuntimeException("Solicitud no encontrada");
        }

        Solicitud solicitud = optSolicitud.get();
        solicitud.rechazar(motivo);

        return solicitudRepository.save(solicitud);
    }

    public long contarSolicitudesPendientes(Long aspiranteId) {
        return solicitudRepository.countByAspiranteIdAndEstado(aspiranteId, EstadoSolicitud.PENDIENTE);
    }

    public long contarEntrevistasPendientes(Long aspiranteId) {
        List<Solicitud> solicitudes = solicitudRepository.findByAspiranteIdOrderByFechaSolicitudDesc(aspiranteId);
        return solicitudes.stream()
                .filter(s -> s.getFechaEntrevista() != null &&
                        s.getFechaEntrevista().isAfter(LocalDateTime.now()))
                .count();
    }
    public boolean yaAplico(Long aspiranteId, Long vacanteId) {
        return solicitudRepository.existsByAspiranteIdAndVacanteId(aspiranteId, vacanteId);
    }




    /**
     * Marcar solicitud como revisada
     */
    public Solicitud marcarComoRevisada(Long solicitudId) {
        Optional<Solicitud> optSolicitud = solicitudRepository.findById(solicitudId);

        if (optSolicitud.isEmpty()) {
            throw new RuntimeException("Solicitud no encontrada");
        }

        Solicitud solicitud = optSolicitud.get();
        solicitud.setEstado(EstadoSolicitud.REVISADA);

        return solicitudRepository.save(solicitud);
    }
}