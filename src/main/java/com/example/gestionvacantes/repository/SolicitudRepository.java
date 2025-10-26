package com.example.gestionvacantes.repository;

import com.example.gestionvacantes.model.Solicitud;
import com.example.gestionvacantes.model.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Solicitud
 */
@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    /**
     * Busca todas las solicitudes de un aspirante
     */
    List<Solicitud> findByAspiranteIdOrderByFechaSolicitudDesc(Long aspiranteId);

    /**
     * Busca todas las solicitudes de una vacante
     */
    List<Solicitud> findByVacanteIdOrderByFechaSolicitudDesc(Long vacanteId);

    /**
     * Busca solicitudes por estado
     */
    List<Solicitud> findByEstado(EstadoSolicitud estado);

    /**
     * Verifica si un aspirante ya aplicó a una vacante
     */
    boolean existsByAspiranteIdAndVacanteId(Long aspiranteId, Long vacanteId);

    /**
     * Busca una solicitud específica de un aspirante a una vacante
     */
    Optional<Solicitud> findByAspiranteIdAndVacanteId(Long aspiranteId, Long vacanteId);

    /**
     * Cuenta solicitudes pendientes de un aspirante
     */
    long countByAspiranteIdAndEstado(Long aspiranteId, EstadoSolicitud estado);

    /**
     * Busca solicitudes con entrevistas programadas que no han sido notificadas
     */
    @Query("SELECT s FROM Solicitud s WHERE s.fechaEntrevista IS NOT NULL AND s.notificacionEnviada = false")
    List<Solicitud> findSolicitudesConEntrevistasSinNotificar();

    /**
     * Busca solicitudes de un empleador (a través de sus vacantes)
     */
    @Query("SELECT s FROM Solicitud s WHERE s.vacante.empleador.id = :empleadorId ORDER BY s.fechaSolicitud DESC")
    List<Solicitud> findByEmpleadorId(@Param("empleadorId") Long empleadorId);

    /**
     * ----> MÉTODO AÑADIDO <----
     * Elimina todas las solicitudes asociadas a un ID de vacante.
     * Es crucial para la eliminación en cascada manual.
     */
    void deleteByVacanteId(Long vacanteId);
}