package br.edu.uniesp.sistemadegestao.internal.pedido.mapper;
import br.edu.uniesp.sistemadegestao.internal.pedido.dto.*;
import br.edu.uniesp.sistemadegestao.internal.pedido.entity.*;
import java.util.List;
public final class PedidoMapper {
    private PedidoMapper() {}
    public static List<ItemPedidoEntity> paraItens(List<ItemPedidoRequest> itens) {
        return itens.stream().map(i -> new ItemPedidoEntity(i.produto(), i.quantidade(), i.valorUnitario())).toList();
    }
    public static PedidoResponse paraResposta(PedidoEntity pedido) {
        return new PedidoResponse(pedido.getId(), pedido.getVersao(), pedido.getCliente().getId(),
                pedido.getCliente().getNome(), pedido.getStatus(), pedido.getStatus().permiteEdicao(),
                pedido.getObservacoes(), pedido.getValorTotal(), pedido.getCriadoEm(), pedido.getAtualizadoEm(),
                pedido.getItens().stream().map(i -> new PedidoResponse.ItemResponse(i.getId(), i.getProduto(),
                        i.getQuantidade(), i.getValorUnitario(), i.getSubtotal())).toList(),
                pedido.getHistorico().stream().map(h -> new PedidoResponse.HistoricoResponse(
                        h.getStatusAnterior(), h.getNovoStatus(), h.getDataAlteracao(),
                        h.getResponsavel().getNome())).toList());
    }
}
