package com.example.gestionvacantes.controller;

import com.example.gestionvacantes.model.Aspirante;
import com.example.gestionvacantes.model.Empleador;
import com.example.gestionvacantes.service.AspiranteService;
import com.example.gestionvacantes.service.EmpleadorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Controlador para autenticación y registro
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AspiranteService aspiranteService;
    private final EmpleadorService empleadorService;

    // Constructor manual (en lugar de @RequiredArgsConstructor)
    public AuthController(AspiranteService aspiranteService,
                          EmpleadorService empleadorService) {
        this.aspiranteService = aspiranteService;
        this.empleadorService = empleadorService;
    }

    /**
     * Muestra el formulario de login
     */
    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(required = false) String error,
                               @RequestParam(required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión exitosamente");
        }
        return "login";
    }

    /**
     * Muestra el formulario de registro de aspirante
     */
    @GetMapping("/registro-aspirante")
    public String mostrarRegistroAspirante(Model model) {
        model.addAttribute("aspirante", new Aspirante());
        return "registro-aspirante";
    }

    /**
     * Procesa el registro de un aspirante
     */
    @PostMapping("/registro-aspirante")
    public String registrarAspirante(@Valid @ModelAttribute("aspirante") Aspirante aspirante,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "registro-aspirante";
        }

        try {
            aspiranteService.registrarAspirante(aspirante);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Registro exitoso. Ya puedes iniciar sesión.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/registro-aspirante";
        }
    }

    /**
     * Muestra el formulario de registro de empleador
     */
    @GetMapping("/registro-empleador")
    public String mostrarRegistroEmpleador(Model model) {
        model.addAttribute("empleador", new Empleador());
        return "registro-empleador";
    }

    /**
     * Procesa el registro de un empleador
     */
    @PostMapping("/registro-empleador")
    public String registrarEmpleador(@Valid @ModelAttribute("empleador") Empleador empleador,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "registro-empleador";
        }

        try {
            empleadorService.registrarEmpleador(empleador);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Registro exitoso. Ya puedes iniciar sesión.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/registro-empleador";
        }
    }

    /**
     * Página de acceso denegado
     */
    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }
}