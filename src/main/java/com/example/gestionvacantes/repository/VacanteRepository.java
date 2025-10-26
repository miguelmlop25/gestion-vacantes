package com.example.gestionvacantes.repository;

import com.example.gestionvacantes.model.Vacante;
import com.example.gestionvacantes.model.enums.EstadoVacante;
import com.example.gestionvacantes.model.enums.TipoTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Vacante
 */
@Repository
public interface VacanteRepository extends JpaRepository<Vacante, Long> {

    /**
     * Busca todas las vacantes de un empleador
     */
    List<Vacante> findByEmpleadorId(Long empleadorId);

    /**
     * Busca vacantes por estado
     */
    List<Vacante> findByEstado(EstadoVacante estado);

    /**
     * Busca vacantes publicadas (para aspirantes)
     */
    List<Vacante> findByEstadoOrderByFechaPublicacionDesc(EstadoVacante estado);

    /**
     * Busca vacantes por ubicación (búsqueda parcial, case insensitive)
     */
    @Query("SELECT v FROM Vacante v WHERE LOWER(v.ubicacion) LIKE LOWER(CONCAT('%', :ubicacion, '%')) AND v.estado = 'PUBLICADA'")
    List<Vacante> buscarPorUbicacion(@Param("ubicacion") String ubicacion);

    /**
     * Busca vacantes por tipo de trabajo
     */
    List<Vacante> findByTipoTrabajoAndEstado(TipoTrabajo tipoTrabajo, EstadoVacante estado);

    /**
     * Busca vacantes por palabra clave en título o descripción
     */
    @Query("SELECT v FROM Vacante v WHERE (LOWER(v.titulo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(v.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND v.estado = 'PUBLICADA' ORDER BY v.fechaPublicacion DESC")
    List<Vacante> buscarPorPalabraClave(@Param("keyword") String keyword);

    /**
     * Búsqueda combinada con filtros
     */
    @Query("SELECT v FROM Vacante v WHERE " +
            "(:ubicacion IS NULL OR LOWER(v.ubicacion) LIKE LOWER(CONCAT('%', :ubicacion, '%'))) AND " +
            "(:tipoTrabajo IS NULL OR v.tipoTrabajo = :tipoTrabajo) AND " +
            "(:keyword IS NULL OR LOWER(v.titulo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(v.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "v.estado = 'PUBLICADA' ORDER BY v.fechaPublicacion DESC")
    List<Vacante> buscarConFiltros(
            @Param("ubicacion") String ubicacion,
            @Param("tipoTrabajo") TipoTrabajo tipoTrabajo,
            @Param("keyword") String keyword
    );
}