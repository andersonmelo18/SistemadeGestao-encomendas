package br.edu.uniesp.sistemadegestao.internal.cliente.dto;

public record CriarClienteResponse(
        Long id,
        String nome,
        String email,
        String celular) {
}
