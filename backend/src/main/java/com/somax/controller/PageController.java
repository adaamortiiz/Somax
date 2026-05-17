package com.somax.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/landing"})
    public String landing() {
        return "landing";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/app/calendar")
    public String calendar() {
        return "calendar";
    }

    @GetMapping("/app/reservas")
    public String reservas() {
        return "reservas";
    }

    @GetMapping("/app/clases")
    public String clases() {
        return "clases";
    }

    @GetMapping("/app/chat")
    public String chat() {
        return "chat";
    }

    @GetMapping("/app/notificaciones")
    public String notificaciones() {
        return "notificaciones";
    }

    @GetMapping("/staff/horarios")
    public String staffHorarios() {
        return "staff-horarios";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/admin/usuarios")
    public String adminUsuarios() {
        return "admin-usuarios";
    }

    @GetMapping("/admin/clases")
    public String adminClases() {
        return "admin-clases";
    }

    @GetMapping("/admin/horarios")
    public String adminHorarios() {
        return "admin-horarios";
    }

    @GetMapping("/admin/reportes")
    public String adminReportes() {
        return "admin-reportes";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "reset-password";
    }
}
