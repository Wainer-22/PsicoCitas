package com.psicocitas;

import com.psicocitas.domain.horario;
import com.psicocitas.domain.paciente;
import com.psicocitas.repository.horarioRepository;
import com.psicocitas.repository.pacienteRepository;
import java.time.LocalTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarDatos(pacienteRepository pacientes,
                                  horarioRepository horarios) {
        return args -> {
            if (!pacientes.existsById(1)) {
                paciente demo = new paciente();
                demo.setIdPaciente(1);
                demo.setNombre("Sofía");
                demo.setApellidos("Martínez");
                demo.setCorreo("sofia@correo.com");
                demo.setTelefono("88888888");
                demo.setActivo(true);
                pacientes.save(demo);
            }

            String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
            for (int psicologo = 1; psicologo <= 3; psicologo++) {
                if (horarios.findByIdPsicologoOrderByIdHorarioAsc(psicologo).isEmpty()) {
                    for (String dia : dias) {
                        horario h = new horario();
                        h.setIdPsicologo(psicologo);
                        h.setDiaSemana(dia);
                        h.setActivo(true);
                        h.setHoraInicio(LocalTime.of(9, 0));
                        h.setHoraFin(LocalTime.of(17, 0));
                        h.setDuracionSesion(50);
                        horarios.save(h);
                    }
                }
            }
        };
    }
}
