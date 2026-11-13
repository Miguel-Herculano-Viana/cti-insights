package br.senai.ctiinsights.dto;

import br.senai.ctiinsights.model.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/** Todos os DTOs (o que entra e sai da API) num lugar só. */
public final class Dtos {
    private Dtos() {}

    // ---------- Auth ----------
    public record CadastroRequest(
            @NotBlank(message = "nome é obrigatório") @Size(max = 120) String nome,
            @NotBlank(message = "e-mail é obrigatório") @Email(message = "e-mail inválido") String email,
            @NotBlank(message = "senha é obrigatória") @Size(min = 6, message = "senha precisa de 6+ caracteres") String senha) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String senha) {}

    public record LoginResponse(String token, UsuarioResponse usuario) {}

    public record UsuarioResponse(Long id, String nome, String email, String perfil, String status, LocalDateTime criadoEm) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(),
                    u.getPerfil().name(), u.getStatus().name(), u.getCriadoEm());
        }
    }

    public record StatusRequest(@NotNull Usuario.Status status) {}

    // ---------- Upload ----------
    public record LinhaCliente(
            Integer numeroLinha,
            String codigoCliente,
            String nomeCliente,
            String consultor,
            String segmento,
            String nivelCliente,
            String faturamentoAnual,
            String servicosContratados,
            String dataContratacao,
            String cidade,
            String uf) {}

    public record UploadRequest(
            @NotBlank String arquivoNome,
            @Min(0) int descartadas,
            @NotEmpty(message = "nenhuma linha enviada") List<@Valid LinhaCliente> linhas) {}

    /** O que o Java manda para o Python (mesmo corpo + quem enviou). */
    public record PedidoPython(String arquivoNome, Long usuarioId, int descartadas, List<LinhaCliente> linhas) {}

    public record LinhaRejeitada(Integer numeroLinha, List<String> motivos) {}
}
