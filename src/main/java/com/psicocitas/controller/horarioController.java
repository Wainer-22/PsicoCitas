package com.psicocitas.controller;

import com.psicocitas.domain.horario;
import com.psicocitas.service.horarioService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/psicologo")
public class horarioController {

    private final horarioService horarioService;

    // Psicólogo de demostración hasta integrar el login.
    private static final Integer ID_PSICOLOGO_DEMO = 1;

    @GetMapping("/disponibilidad")
    public String disponibilidad(Model model) {

        List<horario> horarios =
                horarioService.getHorarios(ID_PSICOLOGO_DEMO);

        if (horarios.isEmpty()) {

            String[] dias = {
                "Lunes", "Martes", "Miércoles",
                "Jueves", "Viernes", "Sábado", "Domingo"
            };

            for (int i = 0; i < dias.length; i++) {
                horario h = new horario();
                h.setDiaSemana(dias[i]);
                h.setIdPsicologo(ID_PSICOLOGO_DEMO);
                h.setActivo(i < 5);
                h.setHoraInicio(LocalTime.of(9, 0));
                h.setHoraFin(LocalTime.of(i == 4 ? 14 : 18, 0));
                h.setDuracionSesion(50);
                horarios.add(h);
            }
        }

        model.addAttribute("horarios", horarios);

        return "psicologo/disponibilidad";
    }

    @PostMapping("/disponibilidad/guardar")
    public String guardar(
            @RequestParam("diaSemana") List<String> dias,
            @RequestParam("horaInicio") List<String> inicios,
            @RequestParam("horaFin") List<String> finales,
            @RequestParam("duracionSesion") Integer duracion,
            @RequestParam(value = "activo",
                    required = false) List<Integer> activos,
            RedirectAttributes redirectAttributes) {

        try {
            List<horario> existentes =
                    horarioService.getHorarios(ID_PSICOLOGO_DEMO);

            List<horario> horarios = new ArrayList<>();

            for (int i = 0; i < dias.size(); i++) {

                horario h;

                if (i < existentes.size()) {
                    h = existentes.get(i);
                } else {
                    h = new horario();
                }

                h.setDiaSemana(dias.get(i));
                h.setActivo(activos != null &&
                        activos.contains(i));
                h.setHoraInicio(LocalTime.parse(inicios.get(i)));
                h.setHoraFin(LocalTime.parse(finales.get(i)));
                h.setDuracionSesion(duracion);

                horarios.add(h);
            }

            horarioService.guardarHorarios(
                    horarios, ID_PSICOLOGO_DEMO);

            redirectAttributes.addFlashAttribute(
                    "mensaje", "Horarios guardados correctamente");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/psicologo/disponibilidad";
    }
}