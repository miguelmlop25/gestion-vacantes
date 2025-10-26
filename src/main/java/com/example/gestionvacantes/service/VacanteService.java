package com.example.gestionvacantes.service;

import com.example.gestionvacantes.model.Vacante;
import com.example.gestionvacantes.model.enums.EstadoVacante;
import com.example.gestionvacantes.model.enums.TipoTrabajo;
import com.example.gestionvacantes.repository.SolicitudRepository; // <-- CAMBIO 1: Importar
import com.example.gestionvacantes.repository.VacanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VacanteService {

    private final VacanteRepository vacanteRepository;
    private final SolicitudRepository solicitudRepository; // <-- CAMBIO 1: Añadir dependencia

    // CAMBIO 2: Actualizar el constructor
    public VacanteService(VacanteRepository vacanteRepository, SolicitudRepository solicitudRepository) {
        this.vacanteRepository = vacanteRepository;
        this.solicitudRepository = solicitudRepository;
    }

    public Vacante crearVacante(Vacante vacante) {
        return vacanteRepository.save(vacante);
    }

    public Vacante actualizarVacante(Vacante vacante) {
        return vacanteRepository.save(vacante);
    }

    public Optional<Vacante> buscarPorId(Long id) {
        return vacanteRepository.findById(id);
    }

    public List<Vacante> obtenerVacantesPorEmpleador(Long empleadorId) {
        return vacanteRepository.findByEmpleadorId(empleadorId);
    }

    public List<Vacante> obtenerVacantesPublicadas() {
        return vacanteRepository.findByEstadoOrderByFechaPublicacionDesc(EstadoVacante.PUBLICADA);
    }

    public List<Vacante> buscarVacantes(String ubicacion, TipoTrabajo tipoTrabajo, String keyword) {
        if ((ubicacion == null || ubicacion.trim().isEmpty()) &&
                tipoTrabajo == null &&
                (keyword == null || keyword.trim().isEmpty())) {
            return obtenerVacantesPublicadas();
        }

        ubicacion = (ubicacion != null && !ubicacion.trim().isEmpty()) ? ubicacion : null;
        keyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;

        return vacanteRepository.buscarConFiltros(ubicacion, tipoTrabajo, keyword);
    }

    public List<Vacante> buscarPorUbicacion(String ubicacion) {
        return vacanteRepository.buscarPorUbicacion(ubicacion);
    }

    public List<Vacante> buscarPorTipoTrabajo(TipoTrabajo tipoTrabajo) {
        return vacanteRepository.findByTipoTrabajoAndEstado(tipoTrabajo, EstadoVacante.PUBLICADA);
    }

    public List<Vacante> buscarPorPalabraClave(String keyword) {
        return vacanteRepository.buscarPorPalabraClave(keyword);
    }

    public void cerrarVacante(Long id) {
        Optional<Vacante> optVacante = vacanteRepository.findById(id);
        if (optVacante.isPresent()) {
            Vacante vacante = optVacante.get();
            vacante.setEstado(EstadoVacante.CERRADA);
            vacanteRepository.save(vacante);
            System.out.println("✅ Vacante cerrada: ID " + id);
        }
    }

    // CAMBIO 3: Lógica de eliminación completamente reemplazada
    @Transactional
    public void eliminarVacante(Long id) {
        // 1. Verificamos que la vacante exista
        if (!vacanteRepository.existsById(id)) {
            throw new RuntimeException("Error: No se encontró la vacante con ID " + id + " para eliminar.");
        }

        // 2. Eliminamos explícitamente todas las solicitudes asociadas a esta vacante
        solicitudRepository.deleteByVacanteId(id);

        // 3. Ahora que no tiene dependencias, eliminamos la vacante
        vacanteRepository.deleteById(id);
        System.out.println("✅ Vacante eliminada: ID " + id);
    }
}