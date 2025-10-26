package com.example.gestionvacantes.model;

import com.example.gestionvacantes.model.enums.EstadoSolicitud;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad Solicitud
 */
@Entity
@Table(name = "solicitudes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aspirante_id", "vacante_id"}))
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aspirante_id", nullable = false)
    private Aspirante aspirante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacante_id", nullable = false)
    private Vacante vacante;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String notaReclutador;

    @Column(nullable = false)
    private String cvAdjunto;

    @Column(nullable = false)
    private Boolean activa = true;

    private LocalDateTime fechaEntrevista;

    @Column(columnDefinition = "TEXT")
    private String detallesEntrevista;

    @Column(nullable = false)
    private Boolean notificacionEnviada = false;

    // Constructor
    public Solicitud() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Aspirante getAspirante() {
        return aspirante;
    }

    public void setAspirante(Aspirante aspirante) {
        this.aspirante = aspirante;
    }

    public Vacante getVacante() {
        return vacante;
    }

    public void setVacante(Vacante vacante) {
        this.vacante = vacante;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public String getNotaReclutador() {
        return notaReclutador;
    }

    public void setNotaReclutador(String notaReclutador) {
        this.notaReclutador = notaReclutador;
    }

    public String getCvAdjunto() {
        return cvAdjunto;
    }

    public void setCvAdjunto(String cvAdjunto) {
        this.cvAdjunto = cvAdjunto;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getFechaEntrevista() {
        return fechaEntrevista;
    }

    public void setFechaEntrevista(LocalDateTime fechaEntrevista) {
        this.fechaEntrevista = fechaEntrevista;
    }

    public String getDetallesEntrevista() {
        return detallesEntrevista;
    }

    public void setDetallesEntrevista(String detallesEntrevista) {
        this.detallesEntrevista = detallesEntrevista;
    }

    public Boolean getNotificacionEnviada() {
        return notificacionEnviada;
    }

    public void setNotificacionEnviada(Boolean notificacionEnviada) {
        this.notificacionEnviada = notificacionEnviada;
    }

    // Métodos de negocio
    public void programarEntrevista(LocalDateTime fecha, String detalles) {
        this.fechaEntrevista = fecha;
        this.detallesEntrevista = detalles;
        this.estado = EstadoSolicitud.ACEPTADA;
        this.notificacionEnviada = false;
    }

    public void rechazar(String motivo) {
        this.estado = EstadoSolicitud.RECHAZADA;
        this.notaReclutador = motivo;
    }
}