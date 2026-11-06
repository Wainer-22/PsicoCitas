package com.psicocitas.service;

import com.psicocitas.domain.horario;
import com.psicocitas.repository.horarioRepository;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class horarioService {

    private final horarioRepository horarioRepository;

    @Transactional(readOnly = true)
    public List<horario> getHorarios(Integer idPsicologo) {
        return horarioRepository
                .findByIdPsicologoOrderByIdHorarioAsc(idPsicologo);
    }

    @Transactional
    public void guardarHorarios(List<horario> horarios,
            Integer idPsicologo) {

        for (horario h : horarios) {

            if (h.getIdHorario() != null) {
                horario guardado = horarioRepository
                        .findById(h.getIdHorario())
                        .orElseThrow();

                if (!guardado.getIdPsicologo()
                        .equals(idPsicologo)) {
                    throw new IllegalArgumentException(
                            "El horario no pertenece al psicólogo");
                }
            }

            h.setIdPsicologo(idPsicologo);

            if (h.isActivo()) {
                LocalTime inicio = h.getHoraInicio();
                LocalTime fin = h.getHoraFin();

                if (inicio == null || fin == null ||
                        !inicio.isBefore(fin)) {
                    throw new IllegalArgumentException(
                            "Las horas del horario no son válidas");
                }
            }

            if (h.getDuracionSesion() == null ||
                    h.getDuracionSesion() <= 0) {
                throw new IllegalArgumentException(
                        "La duración de sesión no es válida");
            }

            horarioRepository.save(h);
        }
    }
}