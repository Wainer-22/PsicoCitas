package com.psicocitas.service;

import com.psicocitas.domain.usuario;
import com.psicocitas.repository.usuarioRepository;
import com.psicocitas.repository.pacienteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class usuarioService {

    private final usuarioRepository usuarioRepository;
    private final pacienteRepository pacienteRepository;

    @Transactional(readOnly = true)
    public List<usuario> getUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public usuario getUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta seleccionada no existe"));
    }

    @Transactional
    public void guardar(usuario cuenta, String contrasenaNueva) {
        cuenta.setCorreo(cuenta.getCorreo().trim().toLowerCase());
        cuenta.setRol(cuenta.getRol().toUpperCase());
        if (!List.of("PACIENTE", "PSICOLOGO", "ADMINISTRADOR").contains(cuenta.getRol())) {
            throw new IllegalArgumentException("Seleccione un rol válido");
        }
        boolean correoDuplicado = cuenta.getIdUsuario() == null
                ? usuarioRepository.existsByCorreoIgnoreCase(cuenta.getCorreo())
                : usuarioRepository.existsByCorreoIgnoreCaseAndIdUsuarioNot(cuenta.getCorreo(), cuenta.getIdUsuario());
        if (correoDuplicado) {
            throw new IllegalArgumentException("El correo ya se encuentra registrado");
        }
        if (contrasenaNueva != null && !contrasenaNueva.isBlank()) {
            if (contrasenaNueva.length() < 8) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
            }
            cuenta.setContrasena(hashContrasena(contrasenaNueva));
        } else if (cuenta.getIdUsuario() == null) {
            throw new IllegalArgumentException("La contraseña es obligatoria para una cuenta nueva");
        }
        usuarioRepository.save(cuenta);
    }

    @Transactional
    public void registrarPaciente(String nombre, String apellidos, String correo, String telefono, String contrasena) {
        String normalizado = correo.trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(normalizado)) {
            throw new IllegalArgumentException("El correo ya se encuentra registrado");
        }
        usuario cuenta = new usuario();
        cuenta.setNombre(nombre.trim());
        cuenta.setApellidos(apellidos.trim());
        cuenta.setCorreo(normalizado);
        cuenta.setRol("PACIENTE");
        cuenta.setActivo(true);
        guardar(cuenta, contrasena);
        com.psicocitas.domain.paciente paciente = new com.psicocitas.domain.paciente();
        paciente.setNombre(nombre.trim());
        paciente.setApellidos(apellidos.trim());
        paciente.setCorreo(normalizado);
        paciente.setTelefono(telefono.trim());
        paciente.setActivo(true);
        pacienteRepository.save(paciente);
    }

    @Transactional(readOnly = true)
    public usuario autenticar(String correo, String contrasena) {
        return usuarioRepository.findByCorreoIgnoreCase(correo.trim())
                .filter(usuario::isActivo)
                .filter(cuenta -> verificarContrasena(contrasena, cuenta.getContrasena()))
                .orElse(null);
    }

    private String hashContrasena(String contrasena) {
        try {
            byte[] sal = new byte[16];
            new java.security.SecureRandom().nextBytes(sal);
            int iteraciones = 210_000;
            var especificacion = new javax.crypto.spec.PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, 256);
            byte[] hash = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();
            return "pbkdf2$" + iteraciones + "$"
                    + java.util.Base64.getEncoder().encodeToString(sal) + "$"
                    + java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("No fue posible proteger la contraseña", e);
        }
    }

    private boolean verificarContrasena(String contrasena, String almacenada) {
        if (contrasena == null || almacenada == null) {
            return false;
        }
        try {
            String[] partes = almacenada.split("\\$");
            if (partes.length != 4 || !"pbkdf2".equals(partes[0])) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = java.util.Base64.getDecoder().decode(partes[2]);
            byte[] esperado = java.util.Base64.getDecoder().decode(partes[3]);
            var especificacion = new javax.crypto.spec.PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, esperado.length * 8);
            byte[] calculado = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();
            return java.security.MessageDigest.isEqual(esperado, calculado);
        } catch (java.security.GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }
}
