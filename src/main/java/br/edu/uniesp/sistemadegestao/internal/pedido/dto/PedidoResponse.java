package br.edu.uniesp.sistemadegestao.internal.pedido.dto;
import br.edu.uniesp.sistemadegestao.internal.pedido.entity.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record PedidoResponse(
        Long id, Long versao, Long clienteId, String clienteNome, StatusPedido status,
        boolean permiteEdicao, String observacoes, BigDecimal valorTotal,
        LocalDateTime criadoEm, LocalDateTime atualizadoEm,
        List<ItemResponse> itens, List<HistoricoResponse> historico) {
    public record ItemResponse(Long id, String produto, int quantidade,
                               BigDecimal valorUnitario, BigDecimal subtotal) {}
    public record HistoricoResponse(StatusPedido statusAnterior, StatusPedido novoStatus,
                                    LocalDateTime dataAlteracao, String responsavel) {}
}
