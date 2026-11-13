package br.senai.ctiinsights.controller;

import br.senai.ctiinsights.dto.Dtos.UploadRequest;
import br.senai.ctiinsights.security.UsuarioLogado;
import br.senai.ctiinsights.service.UploadService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService service;

    public UploadController(UploadService service) {
        this.service = service;
    }

    /** Resposta: {loteId, rejeitadas, resumo, qualidade} (vinda do Python). */
    @PostMapping("/validadas")
    @ResponseStatus(HttpStatus.CREATED)
    public JsonNode enviar(@Valid @RequestBody UploadRequest req,
                           @AuthenticationPrincipal UsuarioLogado logado) {
        return service.enviar(req, logado.id());
    }
}
