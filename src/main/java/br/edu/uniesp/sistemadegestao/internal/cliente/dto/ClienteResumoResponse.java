package br.edu.uniesp.sistemadegestao.internal.cliente.dto;

public record ClienteResumoResponse(
        Long id,
        String nome,
        String cpf,
        String email,
        boolean ativo,
        String celular
) {
}

