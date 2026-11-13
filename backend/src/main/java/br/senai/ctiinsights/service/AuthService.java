package br.senai.ctiinsights.service;

import br.senai.ctiinsights.controller.ApiException;
import br.senai.ctiinsights.dto.Dtos.*;
import br.senai.ctiinsights.model.Usuario;
import br.senai.ctiinsights.repository.UsuarioRepository;
import br.senai.ctiinsights.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** Cria a conta como PENDENTE: só entra depois que um admin aprovar. */
    public UsuarioResponse cadastrar(CadastroRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "e-mail já cadastrado");
        }
        Usuario u = new Usuario(req.nome().trim(), email, encoder.encode(req.senha()));
        return UsuarioResponse.de(usuarios.save(u));
    }

    public LoginResponse login(LoginRequest req) {
        Usuario u = usuarios.findByEmailIgnoreCase(req.email().trim())
                .filter(x -> encoder.matches(req.senha(), x.getSenhaHash()))
                // mesma mensagem para e-mail e senha errados: não revela quais e-mails existem
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "e-mail ou senha incorretos"));

        switch (u.getStatus()) {
            case PENDENTE -> throw new ApiException(HttpStatus.FORBIDDEN, "cadastro aguardando aprovação do administrador");
            case BLOQUEADO -> throw new ApiException(HttpStatus.FORBIDDEN, "usuário bloqueado");
            case APROVADO -> { }
        }
        return new LoginResponse(jwt.gerar(u), UsuarioResponse.de(u));
    }

    public UsuarioResponse buscar(Long id) {
        return usuarios.findById(id).map(UsuarioResponse::de)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "usuário não encontrado"));
    }
}
