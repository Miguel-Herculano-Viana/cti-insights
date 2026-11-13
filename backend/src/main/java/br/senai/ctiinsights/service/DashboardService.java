package br.senai.ctiinsights.service;

import br.senai.ctiinsights.controller.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lê o que o Python gravou. Usa JdbcTemplate (SQL direto) porque aqui só
 * lemos um JSON pronto; não vale criar entidades JPA para isso.
 */
@Service
public class DashboardService {

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public DashboardService(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /** Histórico de envios (mais novo primeiro). */
    public List<Map<String, Object>> listarLotes() {
        return jdbc.query("""
                SELECT l.id, l.arquivo_nome, l.criado_em, l.linhas_recebidas, l.linhas_validas,
                       l.linhas_invalidas, u.nome AS enviado_por
                FROM upload_lote l
                LEFT JOIN usuario u ON u.id = l.usuario_id
                ORDER BY l.id DESC
                LIMIT 50
                """, (rs, i) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rs.getLong("id"));
            m.put("arquivoNome", rs.getString("arquivo_nome"));
            m.put("criadoEm", rs.getTimestamp("criado_em").toLocalDateTime());
            m.put("linhasRecebidas", rs.getInt("linhas_recebidas"));
            m.put("linhasValidas", rs.getInt("linhas_validas"));
            m.put("linhasInvalidas", rs.getInt("linhas_invalidas"));
            m.put("enviadoPor", rs.getString("enviado_por"));
            return m;
        });
    }

    /** Estatísticas de um lote; se loteId for null, do mais recente. */
    public Map<String, Object> estatisticas(Long loteId) {
        String sql = """
                SELECT l.id, l.arquivo_nome, e.gerado_em, e.resultado::text AS resultado
                FROM upload_lote l
                JOIN estatistica_lote e ON e.lote_id = l.id
                """ + (loteId == null ? "ORDER BY l.id DESC LIMIT 1" : "WHERE l.id = ?");
        Object[] params = loteId == null ? new Object[]{} : new Object[]{loteId};

        List<Map<String, Object>> achados = jdbc.query(sql, (rs, i) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("lote", Map.of("id", rs.getLong("id"), "arquivoNome", rs.getString("arquivo_nome")));
            m.put("geradoEm", rs.getTimestamp("gerado_em").toLocalDateTime());
            try {
                m.put("estatisticas", json.readTree(rs.getString("resultado")));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("JSON inválido em estatistica_lote", e);
            }
            return m;
        }, params);

        if (achados.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    loteId == null ? "nenhuma planilha enviada ainda" : "lote não encontrado");
        }
        return achados.get(0);
    }
}
