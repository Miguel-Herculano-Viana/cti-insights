package br.senai.ctiinsights.controller;

import br.senai.ctiinsights.dto.Dtos.*;
import br.senai.ctiinsights.model.Usuario;
import br.senai.ctiinsights.security.UsuarioLogado;
import br.senai.ctiinsights.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Só ADMIN chega aqui (regra no SecurityConfig: /api/admin/**). */
@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService service;

    public AdminUsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) Usuario.Status status) {
        return service.listar(status);
    }

    @PatchMapping("/{id}/status")
    public UsuarioResponse mudarStatus(@PathVariable Long id,
                                       @Valid @RequestBody StatusRequest req,
                                       @AuthenticationPrincipal UsuarioLogado admin) {
        return service.mudarStatus(id, req.status(), admin.id());
    }
}
