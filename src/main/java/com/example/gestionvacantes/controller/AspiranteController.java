package com.example.gestionvacantes.controller;

import com.example.gestionvacantes.model.Aspirante;
import com.example.gestionvacantes.model.Solicitud;
import com.example.gestionvacantes.model.Vacante;
import com.example.gestionvacantes.model.enums.TipoTrabajo;
import com.example.gestionvacantes.service.AspiranteService;
import com.example.gestionvacantes.service.SolicitudService;
import com.example.gestionvacantes.service.VacanteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional; // Asegúrate de tener este import

/**
 * Controlador para funcionalidades de aspirantes
 */
@Controller
@RequestMapping("/aspirante")
public class AspiranteController {

    private final AspiranteService aspiranteService;
    private final VacanteService vacanteService;
    private final SolicitudService solicitudService;

    public AspiranteController(AspiranteService aspiranteService,
                               VacanteService vacanteService,
                               SolicitudService solicitudService) {
        this.aspiranteService = aspiranteService;
        this.vacanteService = vacanteService;
        this.solicitudService = solicitudService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Aspirante aspirante = obtenerAspiranteAutenticado(authentication);
        long solicitudesPendientes = solicitudService.contarSolicitudesPendientes(aspirante.getId());
        long entrevistasPendientes = solicitudService.contarEntrevistasPendientes(aspirante.getId());
        List<Solicitud> ultimasSolicitudes = solicitudService
                .obtenerSolicitudesPorAspirante(aspirante.getId())
                .stream()
                .limit(5)
                .toList();
        model.addAttribute("aspirante", aspirante);
        model.addAttribute("solicitudesPendientes", solicitudesPendientes);
        model.addAttribute("entrevistasPendientes", entrevistasPendientes);
        model.addAttribute("ultimasSolicitudes", ultimasSolicitudes);
        return "aspirante/dashboard";
    }

    @GetMapping("/buscar-vacantes")
    public String buscarVacantes(@RequestParam(required = false) String ubicacion,
                                 @RequestParam(required = false) TipoTrabajo tipoTrabajo,
                                 @RequestParam(required = false) String keyword,
                                 Model model) {
        List<Vacante> vacantes = vacanteService.buscarVacantes(ubicacion, tipoTrabajo, keyword);
        model.addAttribute("vacantes", vacantes);
        model.addAttribute("ubicacion", ubicacion);
        model.addAttribute("tipoTrabajo", tipoTrabajo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("tiposTrabajo", TipoTrabajo.values());
        return "aspirante/buscar-vacantes";
    }

    // ----> MÉTODO 'verVacante' COMPLETAMENTE CORREGIDO <----
    @GetMapping("/vacante/{id}")
    public String verVacante(@PathVariable Long id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        Optional<Vacante> optVacante = vacanteService.buscarPorId(id);
        if (optVacante.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La vacante que buscas no existe.");
            return "redirect:/aspirante/buscar-vacantes";
        }

        Aspirante aspirante = obtenerAspiranteAutenticado(authentication);
        Vacante vacante = optVacante.get();

        // Lógica corregida: verificar si existe una solicitud para ESTE aspirante y ESTA vacante
        boolean yaAplico = solicitudService.yaAplico(aspirante.getId(), vacante.getId());

        model.addAttribute("vacante", vacante);
        model.addAttribute("yaAplico", yaAplico);

        // Esta línea ahora encontrará el archivo 'ver-vacante.html' que creaste
        return "aspirante/ver-vacante";
    }

    // ----> MÉTODO 'aplicarVacante' AJUSTADO PARA EL NUEVO FORMULARIO <----
    @PostMapping("/aplicar")
    public String aplicarVacante(@RequestParam("vacanteId") Long vacanteId,
                                 @RequestParam("cvFile") MultipartFile cvFile,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (cvFile.isEmpty() || cvFile.getSize() == 0) {
                redirectAttributes.addFlashAttribute("error", "Debes adjuntar tu CV en formato PDF.");
                return "redirect:/aspirante/vacante/" + vacanteId;
            }

            Aspirante aspirante = obtenerAspiranteAutenticado(authentication);
            Vacante vacante = vacanteService.buscarPorId(vacanteId)
                    .orElseThrow(() -> new RuntimeException("La vacante no fue encontrada."));

            solicitudService.crearSolicitud(aspirante, vacante, cvFile);

            redirectAttributes.addFlashAttribute("mensaje", "¡Solicitud enviada exitosamente! El empleador revisará tu perfil.");
            return "redirect:/aspirante/mis-solicitudes";

        } catch (IllegalStateException e) { // Captura el error específico si ya aplicó
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/aspirante/vacante/" + vacanteId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al procesar tu solicitud.");
            return "redirect:/aspirante/vacante/" + vacanteId;
        }
    }

    @GetMapping("/mis-solicitudes")
    public String misSolicitudes(Authentication authentication, Model model) {
        Aspirante aspirante = obtenerAspiranteAutenticado(authentication);
        List<Solicitud> solicitudes = solicitudService.obtenerSolicitudesPorAspirante(aspirante.getId());
        model.addAttribute("solicitudes", solicitudes);
        return "aspirante/mis-solicitudes";
    }

    @GetMapping("/solicitud/{id}")
    public String verSolicitud(@PathVariable Long id,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Solicitud solicitud = solicitudService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        Aspirante aspirante = obtenerAspiranteAutenticado(authentication);
        if (!solicitud.getAspirante().getId().equals(aspirante.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver esta solicitud");
            return "redirect:/aspirante/mis-solicitudes";
        }
        model.addAttribute("solicitud", solicitud);
        return "aspirante/ver-solicitud";
    }


    // ... (después de tu método verSolicitud o al final, antes del método auxiliar)

    /**
     * Muestra el formulario para editar el perfil del aspirante
     */
    @GetMapping("/perfil/editar")
    public String mostrarFormularioEditarPerfil(Authentication authentication, Model model) {
        Aspirante aspirante = obtenerAspiranteAutenticado(authentication);
        model.addAttribute("aspirante", aspirante);
        return "aspirante/editar-perfil";
    }

    /**
     * Procesa la actualización del perfil del aspirante
     */
    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam("habilidades") String habilidades,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            Aspirante aspirante = obtenerAspiranteAutenticado(authentication);
            aspiranteService.actualizarHabilidades(aspirante.getId(), habilidades);
            redirectAttributes.addFlashAttribute("mensaje", "¡Tu perfil ha sido actualizado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error al actualizar tu perfil.");
        }
        return "redirect:/aspirante/dashboard";
    }

    private Aspirante obtenerAspiranteAutenticado(Authentication authentication) {
        String correo = authentication.getName();
        return aspiranteService.buscarPorCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Aspirante no encontrado"));
    }
}