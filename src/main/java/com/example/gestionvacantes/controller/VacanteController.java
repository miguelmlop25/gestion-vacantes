package com.example.gestionvacantes.controller;

import com.example.gestionvacantes.model.Vacante;
import com.example.gestionvacantes.service.VacanteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vacantes")
public class VacanteController {

    private final VacanteService vacanteService;

    // Constructor manual
    public VacanteController(VacanteService vacanteService) {
        this.vacanteService = vacanteService;
    }

    @GetMapping("/{id}")
    public String verVacante(@PathVariable Long id, Model model) {
        Vacante vacante = vacanteService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Vacante no encontrada"));

        model.addAttribute("vacante", vacante);

        return "vacante-detalle";
    }
}