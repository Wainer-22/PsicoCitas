package com.psicocitas.repository;

import com.psicocitas.domain.paciente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface pacienteRepository
        extends JpaRepository<paciente, Integer> {

    public List<paciente> findByActivoTrue();

    public boolean existsByCorreo(String correo);
}