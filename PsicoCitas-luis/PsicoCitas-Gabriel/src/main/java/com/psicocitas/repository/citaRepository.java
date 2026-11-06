
package com.psicocitas.repository;

import com.psicocitas.domain.cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface citaRepository
        extends JpaRepository<cita, Integer> {

    List<cita> findByIdPsicologoAndFechaBetweenOrderByFechaAscHoraAsc(
            Integer idPsicologo,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    List<cita> findByIdPacienteOrderByFechaDescHoraDesc(
            Integer idPaciente
    );

    List<cita> findByIdPacienteAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(
            Integer idPaciente,
            LocalDate fecha
    );

    // Citas de un psicólogo en una fecha con un estado (horas ocupadas)
    List<cita> findByIdPsicologoAndFechaAndEstado(
            Integer idPsicologo,
            LocalDate fecha,
            String estado
    );

    // Evita doble reservación en el mismo psicólogo, fecha y hora
    boolean existsByIdPsicologoAndFechaAndHoraAndEstado(
            Integer idPsicologo,
            LocalDate fecha,
            LocalTime hora,
            String estado
    );

    // Evita que un paciente tenga dos citas a la misma fecha y hora
    boolean existsByIdPacienteAndFechaAndHoraAndEstado(
            Integer idPaciente,
            LocalDate fecha,
            LocalTime hora,
            String estado
    );

    // Búsquedas que garantizan que la cita pertenece a quien la consulta
    Optional<cita> findByIdCitaAndIdPaciente(
            Integer idCita,
            Integer idPaciente
    );

    Optional<cita> findByIdCitaAndIdPsicologo(
            Integer idCita,
            Integer idPsicologo
    );
}
