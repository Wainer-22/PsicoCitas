USE psicocitas;

-- El paciente demo y los horarios se crean automáticamente al iniciar Spring Boot.
-- Este registro opcional permite probar el cambio de estado desde la agenda del psicólogo.
-- La fecha queda en el pasado para que se pueda marcar REALIZADA o INASISTENCIA.
INSERT INTO cita (id_paciente, id_psicologo, fecha, hora, modalidad, estado)
SELECT 1, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:00:00', 'Presencial', 'PROGRAMADA'
WHERE EXISTS (SELECT 1 FROM paciente WHERE id_paciente = 1)
  AND NOT EXISTS (
      SELECT 1 FROM cita
      WHERE id_paciente = 1
        AND id_psicologo = 1
        AND fecha = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
        AND hora = '08:00:00'
  );
