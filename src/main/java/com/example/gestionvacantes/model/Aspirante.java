package com.example.gestionvacantes.model;

import com.example.gestionvacantes.model.enums.Role;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aspirantes")
public class Aspirante extends Usuario {

    @Column(columnDefinition = "TEXT")
    private String habilidades;

    @OneToMany(mappedBy = "aspirante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Solicitud> solicitudes = new ArrayList<>();

    public Aspirante() {
        super();
        this.setRol(Role.ASPIRANTE);
    }

    // Getters y Setters
    public String getHabilidades() { return habilidades; }
    public void setHabilidades(String habilidades) { this.habilidades = habilidades; }

    public List<Solicitud> getSolicitudes() { return solicitudes; }
    public void setSolicitudes(List<Solicitud> solicitudes) { this.solicitudes = solicitudes; }

    public void agregarSolicitud(Solicitud solicitud) {
        solicitudes.add(solicitud);
        solicitud.setAspirante(this);
    }

    public void removerSolicitud(Solicitud solicitud) {
        solicitudes.remove(solicitud);
        solicitud.setAspirante(null);
    }
}