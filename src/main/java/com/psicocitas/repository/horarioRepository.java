package com.psicocitas.repository;

import com.psicocitas.domain.horario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface horarioRepository
        extends JpaRepository<horario, Integer> {

    List<horario> findByIdPsicologoOrderByIdHorarioAsc(
            Integer idPsicologo);
}