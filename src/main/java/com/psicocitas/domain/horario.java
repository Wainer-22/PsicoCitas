package com.psicocitas.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "horario")
public class horario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Integer idHorario;

    @Column(name = "id_psicologo", nullable = false)
    private Integer idPsicologo;

    @Column(nullable = false)
    private String diaSemana;

    @Column(nullable = false)
    private boolean activo = false;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    @Column(nullable = false)
    private Integer duracionSesion = 50;
}