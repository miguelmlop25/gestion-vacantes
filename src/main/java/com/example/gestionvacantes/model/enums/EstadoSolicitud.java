package com.example.gestionvacantes.model.enums;

public enum EstadoSolicitud {
    PENDIENTE("Pendiente"),
    REVISADA("Revisada"),
    ACEPTADA("Aceptada"),
    RECHAZADA("Rechazada");

    private final String displayName;

    EstadoSolicitud(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}