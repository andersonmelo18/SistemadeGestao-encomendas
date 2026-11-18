package br.edu.uniesp.sistemadegestao.internal.equipamento.dto;

public record CriarEquipamentoResponse(
        Long id,
        String tipo,
        String marca,
        String modelo,
        String numeroSerie,
        Long clienteId
) {
}
