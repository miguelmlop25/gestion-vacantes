package com.example.gestionvacantes.model;

import com.example.gestionvacantes.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empleadores")
public class Empleador extends Usuario {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Column(nullable = false)
    private String empresa;

    @OneToMany(mappedBy = "empleador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vacante> vacantes = new ArrayList<>();

    public Empleador() {
        super();
        this.setRol(Role.EMPLEADOR);
    }

    // Getters y Setters
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public List<Vacante> getVacantes() { return vacantes; }
    public void setVacantes(List<Vacante> vacantes) { this.vacantes = vacantes; }

    public void agregarVacante(Vacante vacante) {
        vacantes.add(vacante);
        vacante.setEmpleador(this);
    }

    public void removerVacante(Vacante vacante) {
        vacantes.remove(vacante);
        vacante.setEmpleador(null);
    }
}