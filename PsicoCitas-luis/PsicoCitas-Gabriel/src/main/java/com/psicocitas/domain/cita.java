package com.psicocitas.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cita")
public class cita implements Serializable {

    private static final long serialVersionUID = 1L;

    // Estados posibles de una cita
    public static final String PROGRAMADA = "PROGRAMADA";
    public static final String CANCELADA = "CANCELADA";
    public static final String REPROGRAMADA = "REPROGRAMADA";
    public static final String REALIZADA = "REALIZADA";
    public static final String INASISTENCIA = "INASISTENCIA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Integer idCita;

    @Column(name = "id_paciente", nullable = false)
    private Integer idPaciente;

    @Column(name = "id_psicologo", nullable = false)
    private Integer idPsicologo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false)
    private String modalidad;

    @Column(nullable = false)
    private String estado = PROGRAMADA;

    // Datos de cancelación / reprogramación (se conserva el historial)
    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "cancelada_por", length = 20)
    private String canceladaPor;

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    // Si la cita nació de una reprogramación, referencia a la cita original
    @Column(name = "id_cita_origen")
    private Integer idCitaOrigen;

    // Indica si la hora de inicio de la cita ya llegó o pasó.
    // No se guarda en la base de datos (el acceso es por campos).
    public boolean isIniciada() {
        if (fecha == null || hora == null) {
            return false;
        }
        return !LocalDateTime.of(fecha, hora)
                .isAfter(LocalDateTime.now());
    }
}
