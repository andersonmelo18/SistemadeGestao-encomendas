package br.edu.uniesp.sistemadegestao.internal.pedido.entity;

import br.edu.uniesp.sistemadegestao.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.UsuarioEntity;
import br.edu.uniesp.sistemadegestao.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "pedidos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PedidoEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Long versao;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.EM_ANALISE;
    @Column(length = 2000)
    private String observacoes;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorTotal;
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    @Column(nullable = false)
    private LocalDateTime atualizadoEm;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemPedidoEntity> itens = new ArrayList<>();
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private List<HistoricoPedidoEntity> historico = new ArrayList<>();

    public PedidoEntity(ClienteEntity cliente, List<ItemPedidoEntity> itens, String observacoes,
                        UsuarioEntity responsavel) {
        if (!cliente.isAtivo()) throw new RegraNegocioException("Cliente inativo nao pode receber pedidos.");
        this.cliente = cliente;
        this.criadoEm = LocalDateTime.now();
        substituirConteudo(itens, observacoes);
        historico.add(new HistoricoPedidoEntity(this, null, status, responsavel, criadoEm));
    }

    public void editar(List<ItemPedidoEntity> novosItens, String observacoes) {
        if (!status.permiteEdicao())
            throw new RegraNegocioException("Pedido bloqueado para alteracoes a partir de EM_PRODUCAO.");
        substituirConteudo(novosItens, observacoes);
    }

    private void substituirConteudo(List<ItemPedidoEntity> novosItens, String observacoes) {
        if (novosItens == null || novosItens.isEmpty() || novosItens.size() > 100 ||
                novosItens.stream().anyMatch(Objects::isNull))
            throw new RegraNegocioException("Informe de 1 a 100 itens.");
        if (observacoes != null && observacoes.length() > 2000)
            throw new RegraNegocioException("Observacoes devem ter no maximo 2000 caracteres.");
        this.itens.clear();
        novosItens.forEach(item -> { item.associarPedido(this); this.itens.add(item); });
        this.observacoes = observacoes;
        this.valorTotal = novosItens.stream().map(ItemPedidoEntity::getSubtotal)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        this.atualizadoEm = LocalDateTime.now();
    }

    public void transicionar(StatusPedido destino, UsuarioEntity responsavel) {
        if (!status.permiteTransicaoPara(destino))
            throw new RegraNegocioException("Transicao de status invalida.");
        var anterior = status;
        status = destino;
        atualizadoEm = LocalDateTime.now();
        historico.add(new HistoricoPedidoEntity(this, anterior, destino, responsavel, atualizadoEm));
    }

    public List<ItemPedidoEntity> getItens() { return Collections.unmodifiableList(itens); }
    public List<HistoricoPedidoEntity> getHistorico() { return Collections.unmodifiableList(historico); }
}
