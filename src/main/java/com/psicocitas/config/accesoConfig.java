package com.psicocitas.config;

import com.psicocitas.domain.usuario;
import com.psicocitas.repository.usuarioRepository;
import com.psicocitas.service.usuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class accesoConfig implements WebMvcConfigurer {

    private final usuarioService usuarioService;
    private final usuarioRepository usuarioRepository;

    @Value("${psicocitas.admin.correo:}")
    private String correoAdministrador;

    @Value("${psicocitas.admin.contrasena:}")
    private String contrasenaAdministrador;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ControlAcceso(usuarioService))
                .excludePathPatterns("/", "/acceso/login", "/acceso/logout",
                        "/paciente/registro", "/paciente/guardar", "/css/**",
                        "/js/**", "/images/**", "/webjars/**", "/favicon.ico", "/error");
    }

    @Bean
    public ApplicationRunner crearAdministradorInicial() {
        return args -> {
            if (correoAdministrador.isBlank() || contrasenaAdministrador.isBlank()
                    || usuarioRepository.existsByCorreoIgnoreCase(correoAdministrador.trim())) {
                return;
            }
            usuario cuenta = new usuario();
            cuenta.setNombre("Administrador");
            cuenta.setApellidos("PsicoCitas");
            cuenta.setCorreo(correoAdministrador.trim().toLowerCase());
            cuenta.setRol("ADMINISTRADOR");
            cuenta.setActivo(true);
            usuarioService.guardar(cuenta, contrasenaAdministrador);
        };
    }

    private static class ControlAcceso implements HandlerInterceptor {

        private final usuarioService usuarioService;

        private ControlAcceso(usuarioService usuarioService) {
            this.usuarioService = usuarioService;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws IOException {
            HttpSession sesion = request.getSession(false);
            Object idUsuario = sesion == null ? null : sesion.getAttribute("idUsuario");
            if (!(idUsuario instanceof Integer id)) {
                response.sendRedirect(request.getContextPath() + "/acceso/login");
                return false;
            }

            usuario cuenta;
            try {
                cuenta = usuarioService.getUsuario(id);
            } catch (IllegalArgumentException e) {
                sesion.invalidate();
                response.sendRedirect(request.getContextPath() + "/acceso/login?error");
                return false;
            }
            if (!cuenta.isActivo()) {
                sesion.invalidate();
                response.sendRedirect(request.getContextPath() + "/acceso/login?error");
                return false;
            }

            String ruta = request.getRequestURI().substring(request.getContextPath().length());
            String rolRequerido = ruta.startsWith("/administrador/") ? "ADMINISTRADOR"
                    : ruta.startsWith("/psicologo/") ? "PSICOLOGO"
                    : ruta.startsWith("/paciente/") ? "PACIENTE" : null;
            if (rolRequerido != null && !rolRequerido.equals(cuenta.getRol())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            sesion.setAttribute("rol", cuenta.getRol());
            sesion.setAttribute("correo", cuenta.getCorreo());
            return true;
        }
    }
}
