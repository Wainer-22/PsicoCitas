package com.psicocitas.controller;

import com.psicocitas.domain.cita;
import com.psicocitas.domain.paciente;
import com.psicocitas.repository.citaRepository;
import com.psicocitas.repository.pacienteRepository;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class agendaController {

    private final citaRepository citaRepository;
    private final pacienteRepository pacienteRepository;

    // Temporal hasta integrar el inicio de sesión.
    private static final Integer ID_PSICOLOGO_DEMO = 1;

    @GetMapping("/psicologo/agenda")
    public String agenda(
            @RequestParam(defaultValue = "semana") String vista,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            Model model) {

        if (fecha == null) {
            fecha = LocalDate.now();
        }

        if (!vista.equals("dia")
                && !vista.equals("semana")
                && !vista.equals("mes")) {
            vista = "semana";
        }

        LocalDate inicio;
        LocalDate fin;
        LocalDate anterior;
        LocalDate siguiente;

        switch (vista) {
            case "dia" -> {
                inicio = fecha;
                fin = fecha;
                anterior = fecha.minusDays(1);
                siguiente = fecha.plusDays(1);
            }

            case "mes" -> {
                YearMonth mes = YearMonth.from(fecha);
                inicio = mes.atDay(1);
                fin = mes.atEndOfMonth();
                anterior = fecha.minusMonths(1);
                siguiente = fecha.plusMonths(1);
            }

            default -> {
                inicio = fecha.with(DayOfWeek.MONDAY);
                fin = inicio.plusDays(6);
                anterior = fecha.minusWeeks(1);
                siguiente = fecha.plusWeeks(1);
            }
        }

        List<cita> citas = citaRepository
                .findByIdPsicologoAndFechaBetweenOrderByFechaAscHoraAsc(
                        ID_PSICOLOGO_DEMO, inicio, fin);

        List<LocalDate> dias = new ArrayList<>();
        Map<LocalDate, List<cita>> citasPorDia = new HashMap<>();

        LocalDate dia = inicio;

        while (!dia.isAfter(fin)) {
            dias.add(dia);
            citasPorDia.put(dia, new ArrayList<>());
            dia = dia.plusDays(1);
        }

        for (cita c : citas) {
            citasPorDia.get(c.getFecha()).add(c);
        }

        Map<Integer, String> nombresPacientes = new HashMap<>();

        for (cita c : citas) {
            if (!nombresPacientes.containsKey(c.getIdPaciente())) {
                String nombre = pacienteRepository
                        .findById(c.getIdPaciente())
                        .map(p -> p.getNombre() + " " + p.getApellidos())
                        .orElse("Paciente no disponible");

                nombresPacientes.put(c.getIdPaciente(), nombre);
            }
        }

        DateTimeFormatter formato = DateTimeFormatter
                .ofPattern("d MMMM yyyy", new Locale("es", "CR"));

        model.addAttribute("vista", vista);
        model.addAttribute("fecha", fecha);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);
        model.addAttribute("anterior", anterior);
        model.addAttribute("siguiente", siguiente);
        model.addAttribute("dias", dias);
        model.addAttribute("citas", citas);
        model.addAttribute("citasPorDia", citasPorDia);
        model.addAttribute("nombresPacientes", nombresPacientes);
        model.addAttribute("tituloPeriodo",
                inicio.format(formato)
                + (inicio.equals(fin) ? ""
                   : " - " + fin.format(formato)));

        return "psicologo/agenda";
    }
}