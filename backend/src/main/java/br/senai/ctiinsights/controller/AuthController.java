package br.senai.ctiinsights.controller;

import br.senai.ctiinsights.dto.Dtos.*;
import br.senai.ctiinsights.security.UsuarioLogado;
import br.senai.ctiinsights.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/saude")
    public Map<String, String> saude() {
        return Map.of("status", "ok");
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest req) {
        return auth.cadastrar(req);
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @GetMapping("/auth/me")
    public UsuarioResponse eu(@AuthenticationPrincipal UsuarioLogado logado) {
        return auth.buscar(logado.id());
    }
}
