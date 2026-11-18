package br.edu.uniesp.sistemadegestao.internal.tecnico.dto;

public record CriarTecnicoResponse(
        Long id,
        String nome,
        String email,
        String especialidade
) {
}
