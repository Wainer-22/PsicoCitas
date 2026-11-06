package com.psicocitas.controller;

import com.psicocitas.domain.paciente;
import com.psicocitas.service.usuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/paciente")
public class pacienteController {

    private final usuarioService usuarioService;

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("paciente", new paciente());
        return "paciente/registro";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid paciente paciente,
            BindingResult bindingResult,
            @RequestParam("contrasena") String contrasena,
            Model model) {

        if (bindingResult.hasErrors() || contrasena.length() < 8) {
            if (contrasena.length() < 8) {
                model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres");
            }
            return "paciente/registro";
        }

        try {
            usuarioService.registrarPaciente(paciente.getNombre(), paciente.getApellidos(), paciente.getCorreo(), paciente.getTelefono(), contrasena);

            model.addAttribute("mensaje",
                    "Paciente registrado correctamente");

            model.addAttribute("paciente", new paciente());

        } catch (IllegalArgumentException | DataIntegrityViolationException e) {

            model.addAttribute("error",
                    "El correo ya se encuentra registrado");
        }

        return "paciente/registro";
    }
}
