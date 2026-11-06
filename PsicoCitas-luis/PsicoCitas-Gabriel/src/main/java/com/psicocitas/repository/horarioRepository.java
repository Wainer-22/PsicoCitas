package com.psicocitas.repository;

import com.psicocitas.domain.horario;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface horarioRepository
        extends JpaRepository<horario, Integer> {

    List<horario> findByIdPsicologoOrderByIdHorarioAsc(
            Integer idPsicologo);

    // Bloquea los horarios del psicólogo mientras se confirma una
    // reservación, para que dos pacientes no tomen el mismo espacio
    // al mismo tiempo.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from horario h where h.idPsicologo = :idPsicologo")
    List<horario> bloquearPorPsicologo(
            @Param("idPsicologo") Integer idPsicologo);
}
