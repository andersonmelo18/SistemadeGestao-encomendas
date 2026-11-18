package br.edu.uniesp.sistemadegestao.internal.tecnico.dto;

public record TecnicoResumoResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String especialidade,
        boolean ativo
) {
}
