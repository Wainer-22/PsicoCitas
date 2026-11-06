package com.psicocitas.service;

import com.psicocitas.domain.cita;
import com.psicocitas.domain.horario;
import com.psicocitas.repository.citaRepository;
import com.psicocitas.repository.horarioRepository;
import com.psicocitas.repository.pacienteRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class citaService {

    private final citaRepository citaRepository;
    private final horarioRepository horarioRepository;
    private final pacienteRepository pacienteRepository;

    private static final Locale LOCALE_CR =
            Locale.forLanguageTag("es-CR");

    // Minutos de pausa entre una sesión y la siguiente
    // (misma regla que la consulta de disponibilidad).
    private static final int PAUSA_ENTRE_SESIONES = 10;

    private static final List<String> MODALIDADES =
            List.of("Videollamada", "Presencial");

    // Psicólogos de demostración hasta integrar la gestión de usuarios.
    private static final Map<Integer, String> PSICOLOGOS = Map.of(
            1, "Dra. Valentina Ruiz",
            2, "Dr. Mateo Castillo",
            3, "Dra. Lucía Herrera");

    // ---------------------------------------------------------
    // CONSULTAS
    // ---------------------------------------------------------

    public boolean existePsicologo(Integer idPsicologo) {
        return PSICOLOGOS.containsKey(idPsicologo);
    }

    public String getNombrePsicologo(Integer idPsicologo) {
        return PSICOLOGOS.getOrDefault(
                idPsicologo, "Psicólogo no disponible");
    }

    @Transactional(readOnly = true)
    public Optional<cita> getCita(Integer idCita, Integer idPaciente) {
        return citaRepository
                .findByIdCitaAndIdPaciente(idCita, idPaciente);
    }

    // Una cita se puede cancelar/reprogramar si sigue programada
    // y todavía no ha iniciado.
    public boolean puedeModificarse(cita c) {
        return cita.PROGRAMADA.equals(c.getEstado())
                && !c.isIniciada();
    }

    // Horas libres del psicólogo en una fecha: dentro de su horario
    // de atención, sin citas programadas y sin horas pasadas.
    @Transactional(readOnly = true)
    public List<LocalTime> getHorasDisponibles(
            Integer idPsicologo, LocalDate fecha) {

        return calcularHoras(idPsicologo, fecha);
    }

    // ---------------------------------------------------------
    // HU-04: RESERVACIÓN DE CITA
    // ---------------------------------------------------------

    @Transactional
    public cita reservar(Integer idPaciente, Integer idPsicologo,
            LocalDate fecha, LocalTime hora, String modalidad) {

        if (fecha == null || hora == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar una fecha y un horario");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se pueden reservar citas en fechas anteriores");
        }

        if (!existePsicologo(idPsicologo)) {
            throw new IllegalArgumentException(
                    "El psicólogo seleccionado no existe");
        }

        if (modalidad == null || !MODALIDADES.contains(modalidad)) {
            throw new IllegalArgumentException(
                    "Debes seleccionar una modalidad válida");
        }

        if (!pacienteRepository.existsById(idPaciente)) {
            throw new IllegalArgumentException(
                    "El paciente no está registrado en el sistema");
        }

        // Bloquea el horario del psicólogo y verifica de nuevo
        // la disponibilidad antes de confirmar.
        horarioRepository.bloquearPorPsicologo(idPsicologo);

        if (!calcularHoras(idPsicologo, fecha).contains(hora)) {
            throw new IllegalArgumentException(
                    "El horario seleccionado ya no está disponible. "
                    + "Por favor elige otro.");
        }

        if (citaRepository.existsByIdPacienteAndFechaAndHoraAndEstado(
                idPaciente, fecha, hora, cita.PROGRAMADA)) {
            throw new IllegalArgumentException(
                    "Ya tienes una cita programada en esa fecha y hora");
        }

        cita nueva = new cita();
        nueva.setIdPaciente(idPaciente);
        nueva.setIdPsicologo(idPsicologo);
        nueva.setFecha(fecha);
        nueva.setHora(hora);
        nueva.setModalidad(modalidad);
        nueva.setEstado(cita.PROGRAMADA);

        return citaRepository.save(nueva);
    }

    // ---------------------------------------------------------
    // HU-05: CANCELACIÓN DE CITA
    // ---------------------------------------------------------

    @Transactional
    public cita cancelar(Integer idCita, Integer idPaciente,
            String motivo) {

        cita c = citaRepository
                .findByIdCitaAndIdPaciente(idCita, idPaciente)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La cita no existe o no pertenece a tu cuenta"));

        validarModificable(c);

        c.setEstado(cita.CANCELADA);
        c.setFechaCancelacion(LocalDateTime.now());
        c.setCanceladaPor("PACIENTE");
        c.setMotivoCancelacion(limpiarMotivo(motivo));

        // La cita se conserva (historial) y su horario queda libre
        // porque solo las citas PROGRAMADAS ocupan espacio.
        return citaRepository.save(c);
    }

    // ---------------------------------------------------------
    // HU-07: REPROGRAMAR CITA
    // ---------------------------------------------------------

    @Transactional
    public cita reprogramar(Integer idCita, Integer idPaciente,
            LocalDate nuevaFecha, LocalTime nuevaHora) {

        if (nuevaFecha == null || nuevaHora == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un nuevo horario");
        }
        if (nuevaFecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se puede reprogramar para una fecha anterior a hoy");
        }

        cita original = citaRepository
                .findByIdCitaAndIdPaciente(idCita, idPaciente)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La cita no existe o no pertenece a tu cuenta"));

        validarModificable(original);

        if (nuevaFecha.equals(original.getFecha())
                && nuevaHora.equals(original.getHora())) {
            throw new IllegalArgumentException(
                    "Selecciona un horario diferente al de tu cita actual");
        }

        horarioRepository.bloquearPorPsicologo(original.getIdPsicologo());

        if (!calcularHoras(original.getIdPsicologo(), nuevaFecha)
                .contains(nuevaHora)) {
            throw new IllegalArgumentException(
                    "El horario seleccionado ya no está disponible. "
                    + "Por favor elige otro.");
        }

        if (citaRepository.existsByIdPacienteAndFechaAndHoraAndEstado(
                idPaciente, nuevaFecha, nuevaHora, cita.PROGRAMADA)) {
            throw new IllegalArgumentException(
                    "Ya tienes una cita programada en esa fecha y hora");
        }

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // La cita original queda registrada como REPROGRAMADA
        original.setEstado(cita.REPROGRAMADA);
        original.setFechaCancelacion(LocalDateTime.now());
        original.setCanceladaPor("PACIENTE");
        original.setMotivoCancelacion(
                "Reprogramada para el " + nuevaFecha.format(formato)
                + " a las " + nuevaHora);
        citaRepository.save(original);

        // Se agenda la nueva cita con los mismos datos del paciente
        cita nueva = new cita();
        nueva.setIdPaciente(original.getIdPaciente());
        nueva.setIdPsicologo(original.getIdPsicologo());
        nueva.setFecha(nuevaFecha);
        nueva.setHora(nuevaHora);
        nueva.setModalidad(original.getModalidad());
        nueva.setEstado(cita.PROGRAMADA);
        nueva.setIdCitaOrigen(original.getIdCita());

        return citaRepository.save(nueva);
    }

    // ---------------------------------------------------------
    // ACTUALIZACIÓN DE ESTADOS (psicólogo)
    // ---------------------------------------------------------

    @Transactional
    public cita cambiarEstado(Integer idCita, Integer idPsicologo,
            String nuevoEstado) {

        if (!cita.REALIZADA.equals(nuevoEstado)
                && !cita.INASISTENCIA.equals(nuevoEstado)) {
            throw new IllegalArgumentException(
                    "El estado solicitado no es válido");
        }

        cita c = citaRepository
                .findByIdCitaAndIdPsicologo(idCita, idPsicologo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La cita no existe o no está asignada a tu agenda"));

        if (cita.CANCELADA.equals(c.getEstado())) {
            throw new IllegalArgumentException(
                    "Una cita cancelada no puede marcarse como "
                    + "realizada ni como inasistencia");
        }

        if (!cita.PROGRAMADA.equals(c.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se puede actualizar el estado de citas "
                    + "programadas. Estado actual: " + c.getEstado());
        }

        if (!c.isIniciada()) {
            throw new IllegalArgumentException(
                    "La cita aún no ha iniciado, por lo que todavía "
                    + "no se puede actualizar su estado");
        }

        c.setEstado(nuevoEstado);

        return citaRepository.save(c);
    }

    // ---------------------------------------------------------
    // MÉTODOS INTERNOS
    // ---------------------------------------------------------

    private void validarModificable(cita c) {

        if (!cita.PROGRAMADA.equals(c.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se pueden cancelar o reprogramar citas "
                    + "activas y pendientes de realizar");
        }

        if (c.isIniciada()) {
            throw new IllegalArgumentException(
                    "La cita ya inició o ya pasó, por lo que "
                    + "no se puede modificar");
        }
    }

    private String limpiarMotivo(String motivo) {

        if (motivo == null || motivo.isBlank()) {
            return null;
        }

        String limpio = motivo.trim();

        return limpio.length() > 500
                ? limpio.substring(0, 500)
                : limpio;
    }

    private List<LocalTime> calcularHoras(
            Integer idPsicologo, LocalDate fecha) {

        Set<LocalTime> disponibles = new TreeSet<>();

        if (fecha == null) {
            return List.of();
        }

        String diaSemana = fecha.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, LOCALE_CR);

        Set<LocalTime> ocupadas = new TreeSet<>();

        for (cita c : citaRepository.findByIdPsicologoAndFechaAndEstado(
                idPsicologo, fecha, cita.PROGRAMADA)) {
            ocupadas.add(c.getHora());
        }

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        for (horario h : horarioRepository
                .findByIdPsicologoOrderByIdHorarioAsc(idPsicologo)) {

            if (!h.isActivo()
                    || h.getDiaSemana() == null
                    || !h.getDiaSemana().equalsIgnoreCase(diaSemana)
                    || h.getHoraInicio() == null
                    || h.getHoraFin() == null) {
                continue;
            }

            int duracion = h.getDuracionSesion() == null
                    ? 50 : h.getDuracionSesion();

            LocalTime hora = h.getHoraInicio();

            while (true) {

                LocalTime fin = hora.plusMinutes(duracion);

                // fin.isBefore(hora) evita ciclos al pasar de medianoche
                if (fin.isBefore(hora) || fin.isAfter(h.getHoraFin())) {
                    break;
                }

                boolean noPasada = fecha.isAfter(hoy)
                        || (fecha.equals(hoy) && hora.isAfter(ahora));

                if (noPasada && !ocupadas.contains(hora)) {
                    disponibles.add(hora);
                }

                LocalTime siguiente =
                        hora.plusMinutes(duracion + PAUSA_ENTRE_SESIONES);

                if (siguiente.isBefore(hora)) {
                    break;
                }

                hora = siguiente;
            }
        }

        return List.copyOf(disponibles);
    }
}
