
package br.edu.uniesp.sistemadegestao.internal.entrega.dto;

import br.edu.uniesp.sistemadegestao.internal.entrega.entity.StatusEntrega;

import java.time.LocalDateTime;

public record EntregaResponse(
        Long id,
        Long pedidoId,
        Long entregadorId,
        String nomeEntregador,
        StatusEntrega status,
        String enderecoEntrega,
        String observacoes,
        Long versao,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        LocalDateTime saiuEm,
        LocalDateTime entregueEm
) {
}
