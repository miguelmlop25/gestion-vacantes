package com.example.gestionvacantes.service;

import com.example.gestionvacantes.model.Solicitud;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Servicio para envío de correos electrónicos
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    // Constructor manual
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía email de bienvenida al registrarse
     */
    @Async
    public void enviarEmailBienvenida(String destinatario, String nombre, String tipoUsuario) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject("¡Bienvenido al Sistema de Vacantes!");

            String urlDashboard = tipoUsuario.equals("ASPIRANTE") ?
                    baseUrl + "/aspirante/dashboard" :
                    baseUrl + "/empleador/dashboard";

            String contenidoHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #007bff; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f8f9fa; }
                        .button { display: inline-block; padding: 12px 30px; background: #28a745; 
                                 color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>¡Bienvenido al Sistema de Vacantes!</h1>
                        </div>
                        <div class="content">
                            <h2>Hola, %s</h2>
                            <p>Tu cuenta ha sido creada exitosamente. Ya puedes iniciar sesión y comenzar a usar todas las funcionalidades del sistema.</p>
                            <div style="text-align: center;">
                                <a href="%s" class="button">Ir a mi Dashboard</a>
                            </div>
                            <p>También puedes iniciar sesión en: <a href="%s/auth/login">%s/auth/login</a></p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2024 Sistema de Gestión de Vacantes</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(nombre, urlDashboard, baseUrl, baseUrl);

            helper.setText(contenidoHtml, true);
            mailSender.send(message);

            System.out.println("✅ Email de bienvenida enviado a: " + destinatario);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email de bienvenida: " + e.getMessage());
        }
    }

    /**
     * Envía notificación de entrevista a un aspirante
     */
    @Async
    public void enviarNotificacionEntrevista(Solicitud solicitud) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(solicitud.getAspirante().getCorreo());
            helper.setSubject("🎉 ¡Tienes una entrevista programada!");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaFormateada = solicitud.getFechaEntrevista().format(formatter);

            String contenidoHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #28a745; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f8f9fa; }
                        .info-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #007bff; }
                        .button { display: inline-block; padding: 12px 30px; background: #007bff; 
                                 color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>¡Felicitaciones!</h1>
                        </div>
                        <div class="content">
                            <h2>Hola, %s</h2>
                            <p>Tenemos excelentes noticias. Has sido seleccionado para una entrevista.</p>
                            
                            <div class="info-box">
                                <h3>📋 Detalles de la vacante:</h3>
                                <p><strong>Puesto:</strong> %s</p>
                                <p><strong>Empresa:</strong> %s</p>
                            </div>
                            
                            <div class="info-box">
                                <h3>📅 Detalles de la entrevista:</h3>
                                <p><strong>Fecha y hora:</strong> %s</p>
                                <p><strong>Detalles:</strong><br>%s</p>
                            </div>
                            
                            <div style="text-align: center;">
                                <a href="%s/aspirante/mis-solicitudes" class="button">Ver en el sistema</a>
                            </div>
                            
                            <p>Te deseamos mucho éxito en tu entrevista.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2024 Sistema de Gestión de Vacantes</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                    solicitud.getAspirante().getNombre(),
                    solicitud.getVacante().getTitulo(),
                    solicitud.getVacante().getEmpleador().getEmpresa(),
                    fechaFormateada,
                    solicitud.getDetallesEntrevista(),
                    baseUrl
            );

            helper.setText(contenidoHtml, true);
            mailSender.send(message);

            System.out.println("✅ Notificación de entrevista enviada a: " + solicitud.getAspirante().getCorreo());

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar notificación de entrevista: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envía notificación de nueva solicitud a un empleador
     */
    @Async
    public void enviarNotificacionNuevaSolicitud(Solicitud solicitud) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(solicitud.getVacante().getEmpleador().getCorreo());
            helper.setSubject("Nueva solicitud para tu vacante");

            String contenidoHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #007bff; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f8f9fa; }
                        .info-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #28a745; }
                        .button { display: inline-block; padding: 12px 30px; background: #007bff; 
                                 color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Nueva Solicitud Recibida</h1>
                        </div>
                        <div class="content">
                            <h2>Hola, %s</h2>
                            <p>Has recibido una nueva solicitud para tu vacante.</p>
                            
                            <div class="info-box">
                                <h3>📋 Vacante:</h3>
                                <p><strong>%s</strong></p>
                            </div>
                            
                            <div class="info-box">
                                <h3>👤 Aspirante:</h3>
                                <p><strong>Nombre:</strong> %s</p>
                                <p><strong>Habilidades:</strong> %s</p>
                            </div>
                            
                            <div style="text-align: center;">
                                <a href="%s/empleador/ver-interesados/%d" class="button">Ver solicitud</a>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                    solicitud.getVacante().getEmpleador().getNombre(),
                    solicitud.getVacante().getTitulo(),
                    solicitud.getAspirante().getNombre(),
                    solicitud.getAspirante().getHabilidades() != null ?
                            solicitud.getAspirante().getHabilidades() : "No especificadas",
                    baseUrl,
                    solicitud.getVacante().getId()
            );

            helper.setText(contenidoHtml, true);
            mailSender.send(message);

            System.out.println("✅ Notificación de nueva solicitud enviada al empleador");

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}