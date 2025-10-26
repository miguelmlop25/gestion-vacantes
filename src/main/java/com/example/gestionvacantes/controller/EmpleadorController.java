package com.example.gestionvacantes.controller;

import com.example.gestionvacantes.model.Empleador;
import com.example.gestionvacantes.model.Solicitud;
import com.example.gestionvacantes.model.Vacante;
import com.example.gestionvacantes.model.enums.EstadoVacante;
import com.example.gestionvacantes.model.enums.TipoTrabajo;
import com.example.gestionvacantes.service.EmpleadorService;
import com.example.gestionvacantes.service.SolicitudService;
import com.example.gestionvacantes.service.VacanteService;
import com.example.gestionvacantes.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/empleador")
public class EmpleadorController {

    private final EmpleadorService empleadorService;
    private final VacanteService vacanteService;
    private final SolicitudService solicitudService;
    private final FileStorageService fileStorageService;

    // Constructor
    public EmpleadorController(EmpleadorService empleadorService,
                               VacanteService vacanteService,
                               SolicitudService solicitudService,
                               FileStorageService fileStorageService) {
        this.empleadorService = empleadorService;
        this.vacanteService = vacanteService;
        this.solicitudService = solicitudService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Empleador empleador = obtenerEmpleadorAutenticado(authentication);
        List<Vacante> misVacantes = vacanteService.obtenerVacantesPorEmpleador(empleador.getId());
        long vacantesActivas = misVacantes.stream().filter(v -> v.getEstado() == EstadoVacante.PUBLICADA).count();
        long totalSolicitudes = solicitudService.obtenerSolicitudesPorEmpleador(empleador.getId()).size();
        model.addAttribute("empleador", empleador);
        model.addAttribute("vacantesActivas", vacantesActivas);
        model.addAttribute("totalSolicitudes", totalSolicitudes);
        return "empleador/dashboard";
    }

    @GetMapping("/publicar-vacante")
    public String mostrarFormularioPublicar(Model model) {
        model.addAttribute("vacante", new Vacante());
        model.addAttribute("tiposTrabajo", TipoTrabajo.values());
        return "empleador/publicar-vacante";
    }

    @PostMapping("/publicar-vacante")
    public String publicarVacante(@Valid @ModelAttribute("vacante") Vacante vacante,
                                  BindingResult result, Authentication authentication,
                                  RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tiposTrabajo", TipoTrabajo.values());
            return "empleador/publicar-vacante";
        }
        try {
            Empleador empleador = obtenerEmpleadorAutenticado(authentication);
            vacante.setEmpleador(empleador);
            vacante.setEstado(EstadoVacante.PUBLICADA);
            vacanteService.crearVacante(vacante);
            redirectAttributes.addFlashAttribute("mensaje", "¡Vacante publicada exitosamente!");
            return "redirect:/empleador/mis-vacantes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleador/publicar-vacante";
        }
    }

    @GetMapping("/mis-vacantes")
    public String misVacantes(Authentication authentication, Model model) {
        Empleador empleador = obtenerEmpleadorAutenticado(authentication);
        List<Vacante> vacantes = vacanteService.obtenerVacantesPorEmpleador(empleador.getId());
        model.addAttribute("vacantes", vacantes);
        return "empleador/mis-vacantes";
    }

    @GetMapping("/vacante/{id}")
    public String verVacante(@PathVariable Long id, Authentication authentication,
                             Model model, RedirectAttributes redirectAttributes) {
        return vacanteService.buscarPorId(id).map(vacante -> {
            Empleador empleador = obtenerEmpleadorAutenticado(authentication);
            if (!vacante.getEmpleador().getId().equals(empleador.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver esta vacante");
                return "redirect:/empleador/mis-vacantes";
            }
            model.addAttribute("vacante", vacante);
            model.addAttribute("tiposTrabajo", TipoTrabajo.values());
            model.addAttribute("estadosVacante", EstadoVacante.values());
            return "empleador/editar-vacante";
        }).orElse("redirect:/empleador/mis-vacantes");
    }

    @PostMapping("/vacante/actualizar")
    public String actualizarVacante(@Valid @ModelAttribute("vacante") Vacante vacanteActualizada,
                                    BindingResult result,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {

        if (result.hasErrors()) {
            model.addAttribute("tiposTrabajo", TipoTrabajo.values());
            model.addAttribute("estadosVacante", EstadoVacante.values());
            return "empleador/editar-vacante";
        }

        try {
            Vacante vacanteOriginal = vacanteService.buscarPorId(vacanteActualizada.getId())
                    .orElseThrow(() -> new RuntimeException("Vacante no encontrada con ID: " + vacanteActualizada.getId()));

            Empleador empleador = obtenerEmpleadorAutenticado(authentication);
            if (!vacanteOriginal.getEmpleador().getId().equals(empleador.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar esta vacante");
                return "redirect:/empleador/mis-vacantes";
            }

            vacanteOriginal.setTitulo(vacanteActualizada.getTitulo());
            vacanteOriginal.setDescripcion(vacanteActualizada.getDescripcion());
            vacanteOriginal.setRequisitos(vacanteActualizada.getRequisitos());
            vacanteOriginal.setUbicacion(vacanteActualizada.getUbicacion());
            vacanteOriginal.setTipoTrabajo(vacanteActualizada.getTipoTrabajo());
            vacanteOriginal.setSalario(vacanteActualizada.getSalario());
            vacanteOriginal.setEstado(vacanteActualizada.getEstado());

            vacanteService.actualizarVacante(vacanteOriginal);

            redirectAttributes.addFlashAttribute("mensaje", "Vacante actualizada exitosamente");
            return "redirect:/empleador/mis-vacantes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la vacante: " + e.getMessage());
            return "redirect:/empleador/vacante/" + vacanteActualizada.getId();
        }
    }

    @GetMapping("/vacante/eliminar/{id}")
    public String eliminarVacante(@PathVariable("id") Long id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            Vacante vacante = vacanteService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Vacante no encontrada para eliminar"));
            Empleador empleador = obtenerEmpleadorAutenticado(authentication);
            if (!vacante.getEmpleador().getId().equals(empleador.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar esta vacante.");
                return "redirect:/empleador/mis-vacantes";
            }
            vacanteService.eliminarVacante(id);
            redirectAttributes.addFlashAttribute("mensaje", "La vacante ha sido eliminada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la vacante: " + e.getMessage());
        }
        return "redirect:/empleador/mis-vacantes";
    }

    @PostMapping("/vacante/{id}/cerrar")
    public String cerrarVacante(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            vacanteService.cerrarVacante(id);
            redirectAttributes.addFlashAttribute("mensaje", "Vacante cerrada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleador/mis-vacantes";
    }

    @GetMapping("/ver-interesados/{vacanteId}")
    public String verInteresados(@PathVariable Long vacanteId,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        return vacanteService.buscarPorId(vacanteId).map(vacante -> {
            Empleador empleador = obtenerEmpleadorAutenticado(authentication);
            if (!vacante.getEmpleador().getId().equals(empleador.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso");
                return "redirect:/empleador/mis-vacantes";
            }
            List<Solicitud> solicitudes = solicitudService.obtenerSolicitudesPorVacante(vacanteId);
            model.addAttribute("vacante", vacante);
            model.addAttribute("solicitudes", solicitudes);
            return "empleador/ver-interesados";
        }).orElse("redirect:/empleador/mis-vacantes");
    }

    @PostMapping("/programar-entrevista/{solicitudId}")
    public String programarEntrevista(@PathVariable Long solicitudId,
                                      @RequestParam String fechaHora,
                                      @RequestParam String detalles,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            Solicitud solicitud = solicitudService.buscarPorId(solicitudId)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            Empleador empleador = obtenerEmpleadorAutenticado(authentication);

            if (!solicitud.getVacante().getEmpleador().getId().equals(empleador.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso");
                return "redirect:/empleador/mis-vacantes";
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime fechaEntrevista = LocalDateTime.parse(fechaHora, formatter);

            solicitudService.programarEntrevista(solicitudId, fechaEntrevista, detalles);

            redirectAttributes.addFlashAttribute("mensaje",
                    "¡Entrevista programada!");

            return "redirect:/empleador/ver-interesados/" + solicitud.getVacante().getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al programar la entrevista: " + e.getMessage());
            return "redirect:/empleador/dashboard";
        }
    }

    @PostMapping("/rechazar-solicitud/{solicitudId}")
    public String rechazarSolicitud(@PathVariable Long solicitudId,
                                    @RequestParam(required = false) String motivo,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            Solicitud solicitud = solicitudService.buscarPorId(solicitudId)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            Empleador empleador = obtenerEmpleadorAutenticado(authentication);

            if (!solicitud.getVacante().getEmpleador().getId().equals(empleador.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso");
                return "redirect:/empleador/mis-vacantes";
            }

            solicitudService.rechazarSolicitud(solicitudId, motivo);

            redirectAttributes.addFlashAttribute("mensaje", "Solicitud rechazada");

            return "redirect:/empleador/ver-interesados/" + solicitud.getVacante().getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al rechazar la solicitud: " + e.getMessage());
            return "redirect:/empleador/dashboard";
        }
    }

    @GetMapping("/descargar-cv/{solicitudId}")
    public ResponseEntity<Resource> descargarCV(@PathVariable Long solicitudId,
                                                Authentication authentication) {
        try {
            Solicitud solicitud = solicitudService.buscarPorId(solicitudId)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            Empleador empleador = obtenerEmpleadorAutenticado(authentication);

            if (!solicitud.getVacante().getEmpleador().getId().equals(empleador.getId())) {
                // En un endpoint de API es mejor devolver un estado HTTP de error
                return ResponseEntity.status(403).build();
            }

            Path filePath = fileStorageService.obtenerRutaArchivo(solicitud.getCvAdjunto());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Archivo no encontrado o no se puede leer");
            }

            String nombreAspirante = solicitud.getAspirante().getNombre().replace(" ", "_");
            String fileName = "CV_" + nombreAspirante + ".pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }






    /**
     * Marcar solicitud como revisada
     */
    @PostMapping("/marcar-revisada/{solicitudId}")
    public String marcarComoRevisada(@PathVariable Long solicitudId,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        try {
            Solicitud solicitud = solicitudService.buscarPorId(solicitudId)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            Empleador empleador = obtenerEmpleadorAutenticado(authentication);

            if (!solicitud.getVacante().getEmpleador().getId().equals(empleador.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso");
                return "redirect:/empleador/mis-vacantes";
            }

            solicitudService.marcarComoRevisada(solicitudId);

            redirectAttributes.addFlashAttribute("mensaje", "Solicitud marcada como revisada");

            return "redirect:/empleador/ver-interesados/" + solicitud.getVacante().getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleador/dashboard";
        }
    }



    private Empleador obtenerEmpleadorAutenticado(Authentication authentication) {
        String correo = authentication.getName();
        return empleadorService.buscarPorCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Empleador no encontrado"));
    }
}