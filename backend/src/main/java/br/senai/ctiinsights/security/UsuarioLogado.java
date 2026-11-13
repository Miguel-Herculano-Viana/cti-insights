package br.senai.ctiinsights.security;

/** Quem está fazendo a requisição (vira o "principal" do Spring Security). */
public record UsuarioLogado(Long id, String email, String perfil) {}
