package br.senai.ctiinsights.service;

import br.senai.ctiinsights.controller.ApiException;
import br.senai.ctiinsights.dto.Dtos.UsuarioResponse;
import br.senai.ctiinsights.model.Usuario;
import br.senai.ctiinsights.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/** Ações do administrador sobre os usuários. */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public List<UsuarioResponse> listar(Usuario.Status status) {
        List<Usuario> lista = (status == null)
                ? usuarios.findAllByOrderByCriadoEmDesc()
                : usuarios.findByStatusOrderByCriadoEmDesc(status);
        return lista.stream().map(UsuarioResponse::de).toList();
    }

    public UsuarioResponse mudarStatus(Long id, Usuario.Status novo, Long adminId) {
        if (id.equals(adminId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "você não pode mudar o próprio status");
        }
        Usuario u = usuarios.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "usuário não encontrado"));
        u.setStatus(novo);
        return UsuarioResponse.de(usuarios.save(u));
    }
}
