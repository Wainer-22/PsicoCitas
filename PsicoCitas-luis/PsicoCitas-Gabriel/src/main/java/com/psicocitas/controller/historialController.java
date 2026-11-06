package com.psicocitas.controller;

import com.psicocitas.domain.cita;
import com.psicocitas.repository.citaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class historialController {

    private final citaRepository citaRepository;

    // Temporal hasta integrar autenticación.
    private static final Integer ID_PACIENTE_DEMO = 1;

    @GetMapping("/paciente/citas")
    public String misCitas(Model model) {

        List<cita> todas = citaRepository
                .findByIdPacienteOrderByFechaDescHoraDesc(
                        ID_PACIENTE_DEMO);

        List<cita> proximas = new ArrayList<>();
        List<cita> historial = new ArrayList<>();

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        for (cita c : todas) {

            boolean futura = c.getFecha().isAfter(hoy)
                    || (c.getFecha().equals(hoy)
                    && !c.getHora().isBefore(ahora));

            boolean programada =
                    "PROGRAMADA".equalsIgnoreCase(c.getEstado());

            if (futura && programada) {
                proximas.add(c);
            } else {
                historial.add(c);
            }
        }

        proximas.sort((a, b) -> {
            int comparacion =
                    a.getFecha().compareTo(b.getFecha());

            return comparacion != 0
                    ? comparacion
                    : a.getHora().compareTo(b.getHora());
        });

        Map<Integer, String> nombresPsicologos =
                new HashMap<>();

        nombresPsicologos.put(1, "Dra. Valentina Ruiz");
        nombresPsicologos.put(2, "Dr. Mateo Castillo");
        nombresPsicologos.put(3, "Dra. Lucía Herrera");

        model.addAttribute("proximas", proximas);
        model.addAttribute("historial", historial);
        model.addAttribute("cantidadProximas", proximas.size());
        model.addAttribute("cantidadHistorial", historial.size());
        model.addAttribute("nombresPsicologos",
                nombresPsicologos);

        return "paciente/misCitas";
    }
}