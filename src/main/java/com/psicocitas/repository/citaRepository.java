
package com.psicocitas.repository;

import com.psicocitas.domain.cita;
import java.time.LocalDate;
import java.util.List;
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
}
