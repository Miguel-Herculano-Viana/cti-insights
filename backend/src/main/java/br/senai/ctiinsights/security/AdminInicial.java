package br.senai.ctiinsights.security;

import br.senai.ctiinsights.model.Usuario;
import br.senai.ctiinsights.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Ao subir: se o admin configurado no .env não existe, cria já APROVADO. */
@Component
public class AdminInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String nome, email, senha;

    public AdminInicial(UsuarioRepository usuarios, PasswordEncoder encoder,
                        @Value("${app.admin.nome}") String nome,
                        @Value("${app.admin.email}") String email,
                        @Value("${app.admin.senha}") String senha) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (usuarios.existsByEmailIgnoreCase(email)) return;
        Usuario admin = new Usuario(nome, email.toLowerCase(), encoder.encode(senha));
        admin.setPerfil(Usuario.Perfil.ADMIN);
        admin.setStatus(Usuario.Status.APROVADO);
        usuarios.save(admin);
        log.info("Admin inicial criado: {}", email);
    }
}
