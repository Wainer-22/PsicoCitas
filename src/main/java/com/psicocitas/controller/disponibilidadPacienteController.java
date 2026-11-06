package com.psicocitas.controller;

import com.psicocitas.domain.horario;
import com.psicocitas.service.horarioService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class disponibilidadPacienteController {

    private final horarioService horarioService;

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

        String nombrePsicologo = "";

        switch (idPsicologo) {
            case 1 -> nombrePsicologo = "Dra. Valentina Ruiz";
            case 2 -> nombrePsicologo = "Dr. Mateo Castillo";
            case 3 -> nombrePsicologo = "Dra. Lucía Herrera";
            default -> {
                idPsicologo = 1;
                nombrePsicologo = "Dra. Valentina Ruiz";
            }
        }

        List<horario> horarios =
                horarioService.getHorarios(idPsicologo);

        List<LocalTime> horasDisponibles =
                new ArrayList<>();

        String diaSemana = fecha.getDayOfWeek()
                .getDisplayName(
                        java.time.format.TextStyle.FULL,
                        new Locale("es", "CR"));

        for (horario h : horarios) {

            if (h.isActivo()
                    && h.getDiaSemana().equalsIgnoreCase(diaSemana)
                    && h.getHoraInicio() != null
                    && h.getHoraFin() != null) {

                int duracion = h.getDuracionSesion() == null
                        ? 50 : h.getDuracionSesion();

                LocalTime hora = h.getHoraInicio();

                while (!hora.plusMinutes(duracion)
                        .isAfter(h.getHoraFin())) {

                    if (fecha.isAfter(LocalDate.now())
                            || (fecha.equals(LocalDate.now())
                            && hora.isAfter(LocalTime.now()))) {

                        horasDisponibles.add(hora);
                    }

                    hora = hora.plusMinutes(duracion + 10);
                }
            }
        }

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("HH:mm");

        List<String> horas = new ArrayList<>();

        for (LocalTime hora : horasDisponibles) {
            horas.add(hora.format(formato));
        }

        model.addAttribute("idPsicologo", idPsicologo);
        model.addAttribute("nombrePsicologo", nombrePsicologo);
        model.addAttribute("fecha", fecha);
        model.addAttribute("fechaMinima", LocalDate.now());
        model.addAttribute("horas", horas);

        return "paciente/reservar";
    }
}