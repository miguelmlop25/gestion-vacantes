package com.example.gestionvacantes.model;

import com.example.gestionvacantes.model.enums.EstadoVacante;
import com.example.gestionvacantes.model.enums.TipoTrabajo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Vacante
 */
@Entity
@Table(name = "vacantes")
public class Vacante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empleador_id", nullable = false)
    private Empleador empleador;

    @NotBlank(message = "El título es obligatorio")
    @Column(nullable = false)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String requisitos;

    @NotBlank(message = "La ubicación es obligatoria")
    @Column(nullable = false)
    private String ubicacion;

    @NotNull(message = "El tipo de trabajo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTrabajo tipoTrabajo;

    @Column(precision = 10, scale = 2)
    private BigDecimal salario;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion;

    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVacante estado = EstadoVacante.PUBLICADA;

    // ESTA ES LA CONFIGURACIÓN CORRECTA PARA LA ELIMINACIÓN EN CASCADA
    @OneToMany(mappedBy = "vacante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Solicitud> solicitudes = new ArrayList<>();

    // Constructor
    public Vacante() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Empleador getEmpleador() {
        return empleador;
    }

    public void setEmpleador(Empleador empleador) {
        this.empleador = empleador;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public TipoTrabajo getTipoTrabajo() {
        return tipoTrabajo;
    }

    public void setTipoTrabajo(TipoTrabajo tipoTrabajo) {
        this.tipoTrabajo = tipoTrabajo;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public EstadoVacante getEstado() {
        return estado;
    }

    public void setEstado(EstadoVacante estado) {
        this.estado = estado;
    }

    public List<Solicitud> getSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(List<Solicitud> solicitudes) {
        this.solicitudes = solicitudes;
    }

    // Métodos de conveniencia
    public void agregarSolicitud(Solicitud solicitud) {
        solicitudes.add(solicitud);
        solicitud.setVacante(this);
    }

    public void removerSolicitud(Solicitud solicitud) {
        solicitudes.remove(solicitud);
        solicitud.setVacante(null);
    }

    public boolean isActiva() {
        if (estado != EstadoVacante.PUBLICADA) {
            return false;
        }
        if (fechaCierre != null && LocalDateTime.now().isAfter(fechaCierre)) {
            return false;
        }
        return true;
    }
}