package com.psicocitas.controller;

import com.psicocitas.service.citaService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class disponibilidadPacienteController {

    private final citaService citaService;

    @GetMapping("/paciente/reservar")
    public String consultarDisponibilidad(
            @RequestParam(defaultValue = "1") Integer idPsicologo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            Model model) {

        if (fecha == null) {
            fecha = LocalDate.now();
        }

        if (!citaService.existePsicologo(idPsicologo)) {
            idPsicologo = 1;
        }

        // Solo se muestran horas libres: dentro del horario del
        // psicólogo y sin citas programadas.
        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("HH:mm");

        List<String> horas = citaService
                .getHorasDisponibles(idPsicologo, fecha)
                .stream()
                .map(h -> h.format(formato))
                .toList();

        model.addAttribute("idPsicologo", idPsicologo);
        model.addAttribute("nombrePsicologo",
                citaService.getNombrePsicologo(idPsicologo));
        model.addAttribute("fecha", fecha);
        model.addAttribute("fechaMinima", LocalDate.now());
        model.addAttribute("horas", horas);

        return "paciente/reservar";
    }
}
