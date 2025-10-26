package com.example.gestionvacantes.model.enums;

public enum EstadoVacante {
    BORRADOR("Borrador"),
    PUBLICADA("Publicada"),
    CERRADA("Cerrada");

    private final String displayName;

    EstadoVacante(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}