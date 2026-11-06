package com.psicocitas.controller;

import com.psicocitas.domain.usuario;
import com.psicocitas.service.usuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/administrador/usuarios")
public class administradorController {

    private final usuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.getUsuarios());
        return "administrador/usuarios";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new usuario());
        model.addAttribute("edicion", false);
        return "administrador/formularioUsuario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("usuario", usuarioService.getUsuario(id));
        model.addAttribute("edicion", true);
        return "administrador/formularioUsuario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("usuario") usuario cuenta,
            BindingResult resultado, @RequestParam(required = false) String contrasena,
            Model model, RedirectAttributes atributos) {
        boolean edicion = cuenta.getIdUsuario() != null;
        if (resultado.hasErrors()) {
            model.addAttribute("edicion", edicion);
            return "administrador/formularioUsuario";
        }
        try {
            if (edicion) {
                usuario existente = usuarioService.getUsuario(cuenta.getIdUsuario());
                existente.setNombre(cuenta.getNombre());
                existente.setApellidos(cuenta.getApellidos());
                existente.setCorreo(cuenta.getCorreo());
                existente.setRol(cuenta.getRol());
                existente.setActivo(cuenta.isActivo());
                usuarioService.guardar(existente, contrasena);
            } else {
                usuarioService.guardar(cuenta, contrasena);
            }
            atributos.addFlashAttribute("mensaje", edicion ? "Cuenta actualizada correctamente" : "Cuenta creada correctamente");
            return "redirect:/administrador/usuarios";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("edicion", edicion);
            model.addAttribute("error", e instanceof IllegalArgumentException ? e.getMessage() : "El correo ya se encuentra registrado");
            return "administrador/formularioUsuario";
        }
    }
}
