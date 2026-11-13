package br.senai.ctiinsights.security;

import br.senai.ctiinsights.model.Usuario;
import br.senai.ctiinsights.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Roda em toda requisição: se tiver "Authorization: Bearer <token>" válido,
 * marca o usuário como logado. Busca no banco a cada vez para que um usuário
 * bloqueado perca o acesso na hora, mesmo com token ainda válido.
 */
@Component
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UsuarioRepository usuarios;

    public JwtFiltro(JwtService jwt, UsuarioRepository usuarios) {
        this.jwt = jwt;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            jwt.lerUsuarioId(header.substring(7))
                    .flatMap(usuarios::findById)
                    .filter(u -> u.getStatus() == Usuario.Status.APROVADO)
                    .ifPresent(u -> {
                        var logado = new UsuarioLogado(u.getId(), u.getEmail(), u.getPerfil().name());
                        var auth = new UsernamePasswordAuthenticationToken(logado, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + u.getPerfil().name())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        chain.doFilter(req, res);
    }
}
