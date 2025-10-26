package com.example.gestionvacantes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para la página de inicio
 */
@Controller
public class HomeController {

    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }
}