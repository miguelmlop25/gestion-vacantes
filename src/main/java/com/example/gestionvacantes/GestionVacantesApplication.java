package com.example.gestionvacantes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GestionVacantesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionVacantesApplication.class, args);
		System.out.println("Aplicación iniciada en http://localhost:8080");
	}

}