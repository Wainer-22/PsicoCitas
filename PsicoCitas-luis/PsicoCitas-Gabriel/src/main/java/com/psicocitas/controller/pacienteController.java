package com.psicocitas.controller;

import com.psicocitas.domain.paciente;
import com.psicocitas.service.pacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/paciente")
public class pacienteController {

    private final pacienteService pacienteService;

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("paciente", new paciente());
        return "paciente/registro";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid paciente paciente,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "paciente/registro";
        }

        try {
            pacienteService.save(paciente);

            model.addAttribute("mensaje",
                    "Paciente registrado correctamente");

            model.addAttribute("paciente", new paciente());

        } catch (IllegalArgumentException |
                 DataIntegrityViolationException e) {

            model.addAttribute("error",
                    "El correo ya se encuentra registrado");
        }

        return "paciente/registro";
    }
}