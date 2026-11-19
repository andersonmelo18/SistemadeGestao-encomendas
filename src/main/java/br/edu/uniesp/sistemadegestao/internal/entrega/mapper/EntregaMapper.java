
package br.edu.uniesp.sistemadegestao.internal.entrega.mapper;

import br.edu.uniesp.sistemadegestao.internal.entrega.dto.EntregaResponse;
import br.edu.uniesp.sistemadegestao.internal.entrega.entity.EntregaEntity;

public class EntregaMapper {

    private EntregaMapper() {
    }

    public static EntregaResponse paraResposta(EntregaEntity entrega) {
        return new EntregaResponse(
                entrega.getId(),
                entrega.getPedido().getId(),
                entrega.getEntregador().getId(),
                entrega.getEntregador().getNome(),
                entrega.getStatus(),
                entrega.getEnderecoEntrega(),
                entrega.getObservacoes(),
                entrega.getVersao(),
                entrega.getCriadoEm(),
                entrega.getAtualizadoEm(),
                entrega.getSaiuEm(),
                entrega.getEntregueEm()
        );
    }
}
