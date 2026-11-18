package br.edu.uniesp.sistemadegestao.internal.pedido.entity;

import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_pedido")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoricoPedidoEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;
    @Enumerated(EnumType.STRING) @Column(length = 20)
    private StatusPedido statusAnterior;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private StatusPedido novoStatus;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private UsuarioEntity responsavel;
    @Column(nullable = false)
    private LocalDateTime dataAlteracao;

    public HistoricoPedidoEntity(PedidoEntity pedido, StatusPedido anterior, StatusPedido novo,
                                 UsuarioEntity responsavel, LocalDateTime data) {
        this.pedido = pedido;
        this.statusAnterior = anterior;
        this.novoStatus = novo;
        this.responsavel = responsavel;
        this.dataAlteracao = data;
    }
}
