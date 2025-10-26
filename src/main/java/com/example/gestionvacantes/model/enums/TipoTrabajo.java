package com.example.gestionvacantes.model.enums;

public enum TipoTrabajo {
    TIEMPO_COMPLETO("Tiempo Completo"),
    MEDIO_TIEMPO("Medio Tiempo"),
    PRACTICAS("Prácticas"),
    CONTRATO("Contrato"),
    FREELANCE("Freelance");

    private final String displayName;

    TipoTrabajo(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}