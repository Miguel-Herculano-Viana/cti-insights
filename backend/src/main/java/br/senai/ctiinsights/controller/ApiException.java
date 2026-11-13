package br.senai.ctiinsights.controller;

import org.springframework.http.HttpStatus;

/** Erro "esperado" (regra de negócio). Vira {"erro": "..."} com o status certo. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
