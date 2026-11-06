package com.psicocitas.controller;

import com.psicocitas.repository.usuarioRepository;
import com.psicocitas.service.usuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/acceso")
public class accesoController {

    private final usuarioRepository usuarioRepository;
    private final usuarioService usuarioService;

    @GetMapping("/login")
    public String login() {
        return "acceso/login";
    }

    @GetMapping("/inicio")
    public String inicio(HttpSession sesion) {
        Object id = sesion.getAttribute("idUsuario");
        if (!(id instanceof Integer idUsuario)) {
            return "redirect:/acceso/login";
        }
        var cuenta = usuarioRepository.findById(idUsuario).orElseThrow();
        return switch (cuenta.getRol()) {
            case "ADMINISTRADOR" -> "redirect:/administrador/usuarios";
            case "PSICOLOGO" -> "redirect:/psicologo/agenda";
            default -> "redirect:/paciente/citas";
        };
    }

    @PostMapping("/login")
    public String autenticar(@RequestParam String correo, @RequestParam String contrasena,
            HttpServletRequest request) {
        var cuenta = usuarioService.autenticar(correo, contrasena);
        if (cuenta == null) {
            return "redirect:/acceso/login?error";
        }
        HttpSession sesionAnterior = request.getSession(false);
        if (sesionAnterior != null) {
            sesionAnterior.invalidate();
        }
        HttpSession sesion = request.getSession(true);
        sesion.setAttribute("idUsuario", cuenta.getIdUsuario());
        sesion.setAttribute("rol", cuenta.getRol());
        sesion.setAttribute("correo", cuenta.getCorreo());
        return "redirect:/acceso/inicio";
    }

    @PostMapping("/logout")
    public String cerrarSesion(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/acceso/login?logout";
    }
}
