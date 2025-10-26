package com.example.gestionvacantes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Servicio para almacenar archivos (CVs en PDF)
 */
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            System.out.println("✅ Directorio de uploads creado: " + this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", ex);
        }
    }

    /**
     * Almacena un archivo y retorna el nombre único generado
     */
    public String almacenarArchivo(MultipartFile file) {
        // Obtener el nombre original del archivo
        String nombreOriginal = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Validar que el archivo no esté vacío
            if (file.isEmpty()) {
                throw new RuntimeException("El archivo está vacío");
            }

            // Validar que sea un PDF
            if (!nombreOriginal.toLowerCase().endsWith(".pdf")) {
                throw new RuntimeException("Solo se permiten archivos PDF");
            }

            // Validar caracteres inválidos
            if (nombreOriginal.contains("..")) {
                throw new RuntimeException("Nombre de archivo inválido");
            }

            // Generar nombre único
            String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            String nombreUnico = UUID.randomUUID().toString() + extension;

            // Copiar archivo al directorio
            Path targetLocation = this.fileStorageLocation.resolve(nombreUnico);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Archivo almacenado: " + nombreUnico);
            return nombreUnico;

        } catch (IOException ex) {
            throw new RuntimeException("Error al almacenar el archivo " + nombreOriginal, ex);
        }
    }

    /**
     * Obtiene la ruta completa de un archivo
     */
    public Path obtenerRutaArchivo(String nombreArchivo) {
        return fileStorageLocation.resolve(nombreArchivo).normalize();
    }

    /**
     * Elimina un archivo
     */
    public void eliminarArchivo(String nombreArchivo) {
        try {
            Path filePath = fileStorageLocation.resolve(nombreArchivo).normalize();
            Files.deleteIfExists(filePath);
            System.out.println("✅ Archivo eliminado: " + nombreArchivo);
        } catch (IOException ex) {
            System.err.println("❌ Error al eliminar archivo: " + ex.getMessage());
        }
    }
}