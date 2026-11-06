package com.psicocitas.service;

import com.psicocitas.domain.paciente;
import com.psicocitas.repository.pacienteRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class pacienteService {

    private final pacienteRepository pacienteRepository;

    @Transactional(readOnly = true)
    public List<paciente> getPacientes(boolean activo) {
        if (activo) {
            return pacienteRepository.findByActivoTrue();
        } else {
            return pacienteRepository.findAll();
        }
    }

    @Transactional(readOnly = true)
    public Optional<paciente> getPaciente(Integer idPaciente) {
        return pacienteRepository.findById(idPaciente);
    }

    @Transactional
    public void save(paciente paciente) {

        if (pacienteRepository.existsByCorreo(
                paciente.getCorreo())) {
            throw new IllegalArgumentException(
                    "El correo ya se encuentra registrado");
        }

        pacienteRepository.save(paciente);
    }
}