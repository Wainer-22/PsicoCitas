# PsicoCitas - NetBeans / Java 17

Sistema web de gestión de citas psicológicas. Proyecto Maven con Spring Boot 3, Spring Data JPA, Thymeleaf, Bootstrap y MySQL.

## Requisitos
- JDK 17
- NetBeans con soporte Maven
- MySQL Server 8 o compatible
- Conexión a Internet para descargar dependencias Maven y cargar Bootstrap desde CDN

## Configuración inicial
1. En MySQL Workbench ejecute `docs/crear_base_datos.sql` (crea la base `psicocitas`).
2. Abra NetBeans y seleccione **File > Open Project**. Seleccione la carpeta `PsicoCitas-Gabriel`.
3. Revise `src/main/resources/application.properties`. Por defecto utiliza usuario `root` y contraseña vacía.
4. Si su MySQL tiene contraseña, defina `DB_PASSWORD` o cambie `spring.datasource.password`.
5. Ejecute el proyecto desde NetBeans o con `mvn spring-boot:run`.
6. Abra `http://localhost:8080`.

## Datos de demostración
En el primer inicio se crea el paciente demo con ID 1 y se generan horarios de lunes a viernes de 09:00 a 17:00 para los psicólogos 1, 2 y 3. Los nombres de psicólogos se encuentran definidos en `citaService` como datos de demostración.

- Paciente demo: Sofía Martínez
- Correo: sofia@correo.com
- Psicólogos: Dra. Valentina Ruiz (1), Dr. Mateo Castillo (2), Dra. Lucía Herrera (3)

## Funcionalidades implementadas
- HU-04: reservar cita, consultar horarios disponibles, verificar nuevamente disponibilidad antes de guardar y mostrar confirmación.
- HU-05: cancelar citas programadas futuras, solicitar confirmación, registrar motivo/fecha de cancelación y conservar el registro histórico.
- HU-07: reprogramar una cita, mostrar solo horarios libres del psicólogo, confirmar el cambio y conservar la cita original como REPROGRAMADA mientras crea la nueva cita.
- Estados de cita para el psicólogo: REALIZADA e INASISTENCIA, con validaciones.
- Agenda semanal, mensual y diaria; administración de disponibilidad horaria.

## Rutas principales
- `/paciente/citas` - próximas citas e historial.
- `/paciente/reservar` - reservación.
- `/paciente/registro` - registro de paciente.
- `/psicologo/agenda` - agenda y actualización de estados.
- `/psicologo/disponibilidad` - configuración de horarios.

## Nota importante sobre autenticación
Este avance utiliza un paciente y psicólogos de demostración con IDs fijos, tal como estaba planteada la base original del ZIP. No incluye autenticación real ni control de sesiones/roles. Para una entrega que exija inicio de sesión real, debe integrarse Spring Security y asociar cada operación al usuario autenticado; no se debe presentar el modo demo como un sistema de autenticación.
