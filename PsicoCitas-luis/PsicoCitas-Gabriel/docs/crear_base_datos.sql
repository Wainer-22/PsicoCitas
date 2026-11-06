CREATE DATABASE IF NOT EXISTS psicocitas
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Seleccione la base de datos antes de iniciar la aplicación.
USE psicocitas;

-- Las tablas se crean automáticamente con Spring Data JPA / Hibernate.
-- Al iniciar la aplicación se insertan automáticamente:
-- Paciente de demostración ID 1: Sofía Martínez (sofia@correo.com)
-- Horarios de lunes a viernes de 09:00 a 17:00 para psicólogos 1, 2 y 3.
