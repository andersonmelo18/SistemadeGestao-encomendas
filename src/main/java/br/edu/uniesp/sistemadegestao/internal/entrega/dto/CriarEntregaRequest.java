
package br.edu.uniesp.sistemadegestao.internal.entrega.dto;

public record CriarEntregaRequest(
        Long pedidoId,
        Long entregadorId,
        String enderecoEntrega,
        String observacoes
) {
}
