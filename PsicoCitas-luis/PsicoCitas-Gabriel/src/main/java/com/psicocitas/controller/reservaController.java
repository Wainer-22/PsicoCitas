package com.psicocitas.controller;

import com.psicocitas.domain.cita;
import com.psicocitas.service.citaService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/paciente")
public class reservaController {

    private final citaService citaService;

    // Temporal hasta integrar autenticación.
    private static final Integer ID_PACIENTE_DEMO = 1;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ---------------------------------------------------------
    // HU-04: RESERVACIÓN
    // ---------------------------------------------------------

    @PostMapping("/reservar/confirmar")
    public String confirmar(
            @RequestParam("idPsicologo") Integer idPsicologo,
            @RequestParam("fecha")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam(value = "hora", required = false)
            String hora,
            @RequestParam(value = "modalidad", required = false)
            String modalidad,
            RedirectAttributes redirectAttributes) {

        try {
            if (hora == null || hora.isBlank()) {
                throw new IllegalArgumentException(
                        "Debes seleccionar un horario disponible");
            }

            cita c = citaService.reservar(
                    ID_PACIENTE_DEMO, idPsicologo, fecha,
                    LocalTime.parse(hora), modalidad);

            return "redirect:/paciente/reservar/confirmacion/"
                    + c.getIdCita();

        } catch (DateTimeParseException e) {

            redirectAttributes.addFlashAttribute(
                    "error", "El horario seleccionado no es válido");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        redirectAttributes.addAttribute("idPsicologo", idPsicologo);
        redirectAttributes.addAttribute("fecha", fecha);

        return "redirect:/paciente/reservar";
    }

    @GetMapping("/reservar/confirmacion/{idCita}")
    public String confirmacion(
            @PathVariable("idCita") Integer idCita,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<cita> c = citaService.getCita(
                idCita, ID_PACIENTE_DEMO);

        if (c.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "La cita solicitada no existe o no está disponible");
            return "redirect:/paciente/citas";
        }

        model.addAttribute("cita", c.get());
        model.addAttribute("nombrePsicologo",
                citaService.getNombrePsicologo(
                        c.get().getIdPsicologo()));

        return "paciente/confirmacion";
    }

    // ---------------------------------------------------------
    // HU-05: CANCELACIÓN
    // ---------------------------------------------------------

    @PostMapping("/citas/{idCita}/cancelar")
    public String cancelar(
            @PathVariable("idCita") Integer idCita,
            @RequestParam(value = "motivo", required = false)
            String motivo,
            RedirectAttributes redirectAttributes) {

        try {
            citaService.cancelar(idCita, ID_PACIENTE_DEMO, motivo);

            redirectAttributes.addFlashAttribute("mensaje",
                    "La cita fue cancelada correctamente. "
                    + "El horario quedó disponible nuevamente.");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/paciente/citas";
    }

    // ---------------------------------------------------------
    // HU-07: REPROGRAMACIÓN
    // ---------------------------------------------------------

    @GetMapping("/citas/{idCita}/reprogramar")
    public String formularioReprogramar(
            @PathVariable("idCita") Integer idCita,
            @RequestParam(value = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<cita> c = citaService.getCita(
                idCita, ID_PACIENTE_DEMO);

        if (c.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "La cita no existe o no pertenece a tu cuenta");
            return "redirect:/paciente/citas";
        }

        if (!citaService.puedeModificarse(c.get())) {
            redirectAttributes.addFlashAttribute("error",
                    "Solo se pueden reprogramar citas activas "
                    + "y pendientes de realizar");
            return "redirect:/paciente/citas";
        }

        LocalDate hoy = LocalDate.now();

        if (fecha == null) {
            fecha = c.get().getFecha().isBefore(hoy)
                    ? hoy : c.get().getFecha();
        }

        List<String> horas = citaService
                .getHorasDisponibles(c.get().getIdPsicologo(), fecha)
                .stream()
                .map(h -> h.format(DateTimeFormatter.ofPattern("HH:mm")))
                .toList();

        model.addAttribute("cita", c.get());
        model.addAttribute("nombrePsicologo",
                citaService.getNombrePsicologo(
                        c.get().getIdPsicologo()));
        model.addAttribute("fecha", fecha);
        model.addAttribute("fechaMinima", hoy);
        model.addAttribute("horas", horas);

        return "paciente/reprogramar";
    }

    @PostMapping("/citas/{idCita}/reprogramar")
    public String reprogramar(
            @PathVariable("idCita") Integer idCita,
            @RequestParam("fecha")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam(value = "hora", required = false)
            String hora,
            RedirectAttributes redirectAttributes) {

        try {
            if (hora == null || hora.isBlank()) {
                throw new IllegalArgumentException(
                        "Debes seleccionar un nuevo horario disponible");
            }

            cita nueva = citaService.reprogramar(
                    idCita, ID_PACIENTE_DEMO, fecha,
                    LocalTime.parse(hora));

            redirectAttributes.addFlashAttribute("mensaje",
                    "Tu cita fue reprogramada para el "
                    + nueva.getFecha().format(FORMATO_FECHA)
                    + " a las " + nueva.getHora() + ".");

            return "redirect:/paciente/citas";

        } catch (DateTimeParseException e) {

            redirectAttributes.addFlashAttribute(
                    "error", "El horario seleccionado no es válido");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        redirectAttributes.addAttribute("fecha", fecha);

        return "redirect:/paciente/citas/" + idCita + "/reprogramar";
    }
}
