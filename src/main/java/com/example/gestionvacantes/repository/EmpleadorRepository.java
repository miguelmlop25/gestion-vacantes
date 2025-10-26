package com.example.gestionvacantes.repository;

import com.example.gestionvacantes.model.Empleador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Empleador
 */
@Repository
public interface EmpleadorRepository extends JpaRepository<Empleador, Long> {

    /**
     * Busca un empleador por correo electrónico
     */
    Optional<Empleador> findByCorreo(String correo);

    /**
     * Verifica si existe un empleador con el correo dado
     */
    boolean existsByCorreo(String correo);
}