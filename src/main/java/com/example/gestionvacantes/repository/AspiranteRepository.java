package com.example.gestionvacantes.repository;

import com.example.gestionvacantes.model.Aspirante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Aspirante
 * Spring Data JPA genera automáticamente las implementaciones
 */
@Repository
public interface AspiranteRepository extends JpaRepository<Aspirante, Long> {

    /**
     * Busca un aspirante por correo electrónico
     */
    Optional<Aspirante> findByCorreo(String correo);

    /**
     * Verifica si existe un aspirante con el correo dado
     */
    boolean existsByCorreo(String correo);
}