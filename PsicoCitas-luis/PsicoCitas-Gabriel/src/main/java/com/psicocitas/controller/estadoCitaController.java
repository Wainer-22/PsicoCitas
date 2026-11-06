package com.psicocitas.controller;

import com.psicocitas.domain.cita;
import com.psicocitas.service.citaService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/psicologo")
public class estadoCitaController {

    private final citaService citaService;

    // Temporal hasta integrar el inicio de sesión.
    private static final Integer ID_PSICOLOGO_DEMO = 1;

    @PostMapping("/citas/{idCita}/estado")
    public String actualizarEstado(
            @PathVariable("idCita") Integer idCita,
            @RequestParam("estado") String estado,
            @RequestParam(value = "vista", defaultValue = "semana")
            String vista,
            @RequestParam(value = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            RedirectAttributes redirectAttributes) {

        try {
            citaService.cambiarEstado(
                    idCita, ID_PSICOLOGO_DEMO, estado);

            String texto = cita.REALIZADA.equals(estado)
                    ? "realizada"
                    : "inasistencia del paciente";

            redirectAttributes.addFlashAttribute("mensaje",
                    "La cita fue marcada como " + texto
                    + " correctamente.");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        // Regresa a la misma vista y fecha de la agenda
        redirectAttributes.addAttribute("vista", vista);

        if (fecha != null) {
            redirectAttributes.addAttribute("fecha", fecha);
        }

        return "redirect:/psicologo/agenda";
    }
}
