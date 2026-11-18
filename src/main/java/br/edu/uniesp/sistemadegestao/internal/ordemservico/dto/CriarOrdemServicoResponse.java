package br.edu.uniesp.sistemadegestao.internal.ordemservico.dto;

import br.edu.uniesp.sistemadegestao.internal.ordemservico.entity.StatusOrdemServico;

import java.time.LocalDateTime;

public record CriarOrdemServicoResponse(
        Long id,
        String numeroProtocolo,
        StatusOrdemServico status,
        LocalDateTime dataAbertura
) {
}
