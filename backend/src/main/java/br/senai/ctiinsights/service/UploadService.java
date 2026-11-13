package br.senai.ctiinsights.service;

import br.senai.ctiinsights.controller.ApiException;
import br.senai.ctiinsights.dto.Dtos.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Recebe as linhas que o front considerou válidas, confere de novo
 * (nunca confiar só no navegador) e encaminha ao Python.
 */
@Service
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);
    private static final Pattern CODIGO = Pattern.compile("[A-Za-z0-9_-]+");
    private static final Set<String> NIVEIS = Set.of("A", "B", "C");

    private final RestClient python;
    private final String chave;
    private final int maxLinhas;

    public UploadService(@Value("${app.python.url}") String url,
                         @Value("${app.python.chave}") String chave,
                         @Value("${app.upload.max-linhas}") int maxLinhas) {
        var fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(10_000);
        fabrica.setReadTimeout(90_000); // Render grátis demora para "acordar"
        this.python = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
        this.chave = chave;
        this.maxLinhas = maxLinhas;
    }

    public JsonNode enviar(UploadRequest req, Long usuarioId) {
        if (req.linhas().size() > maxLinhas) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "máximo de " + maxLinhas + " linhas por planilha");
        }

        List<String> erros = validar(req.linhas());
        if (!erros.isEmpty()) {
            String resumo = String.join("; ", erros.subList(0, Math.min(3, erros.size())));
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "linhas inválidas: " + resumo);
        }

        var pedido = new PedidoPython(req.arquivoNome(), usuarioId, req.descartadas(), req.linhas());
        try {
            return python.post().uri("/processar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Chave-Interna", chave)
                    .body(pedido)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            log.warn("Python respondeu {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "o serviço de análise recusou os dados");
        } catch (RestClientException e) {
            log.error("Python fora do ar", e);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "serviço de análise indisponível, tente de novo");
        }
    }

    /** Mesmas regras do contrato. Devolve "linha N: motivo" para cada problema. */
    List<String> validar(List<LinhaCliente> linhas) {
        List<String> erros = new ArrayList<>();
        Set<String> codigos = new HashSet<>();

        for (LinhaCliente l : linhas) {
            String n = "linha " + (l.numeroLinha() == null ? "?" : l.numeroLinha());

            if (vazio(l.codigoCliente()) || !CODIGO.matcher(l.codigoCliente().trim()).matches())
                erros.add(n + ": código inválido");
            else if (!codigos.add(l.codigoCliente().trim().toUpperCase()))
                erros.add(n + ": código repetido");

            if (vazio(l.nomeCliente())) erros.add(n + ": nome vazio");
            if (vazio(l.consultor())) erros.add(n + ": consultor vazio");
            if (vazio(l.segmento())) erros.add(n + ": segmento vazio");
            if (vazio(l.servicosContratados())) erros.add(n + ": sem serviços");

            if (vazio(l.nivelCliente()) || !NIVEIS.contains(l.nivelCliente().trim().toUpperCase()))
                erros.add(n + ": nível deve ser A, B ou C");

            try {
                if (vazio(l.faturamentoAnual()) || new BigDecimal(l.faturamentoAnual().trim()).signum() < 0)
                    erros.add(n + ": faturamento inválido");
            } catch (NumberFormatException e) {
                erros.add(n + ": faturamento deve vir como número (ex.: 1850000.00)");
            }

            try {
                if (vazio(l.dataContratacao()) || LocalDate.parse(l.dataContratacao().trim()).getYear() < 1990)
                    erros.add(n + ": data inválida");
            } catch (DateTimeParseException e) {
                erros.add(n + ": data deve vir como aaaa-mm-dd");
            }

            if (!vazio(l.uf()) && !l.uf().trim().matches("[A-Za-z]{2}"))
                erros.add(n + ": UF inválida");
        }
        return erros;
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
