package br.edu.uniesp.sistemadegestao.internal.pedido.entity;

import br.edu.uniesp.sistemadegestao.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "itens_pedido")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemPedidoEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;
    @Column(nullable = false, length = 150)
    private String produto;
    @Column(nullable = false)
    private int quantidade;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    public ItemPedidoEntity(String produto, int quantidade, BigDecimal valorUnitario) {
        if (produto == null || produto.isBlank() || produto.length() > 150 ||
                quantidade < 1 || quantidade > 9999 || valorUnitario == null ||
                valorUnitario.signum() <= 0 || valorUnitario.compareTo(new BigDecimal("99999999.99")) > 0 ||
                valorUnitario.stripTrailingZeros().scale() > 2)
            throw new RegraNegocioException("Produto, quantidade ou valor unitario invalido.");
        this.produto = produto.trim();
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario.setScale(2);
    }

    void associarPedido(PedidoEntity pedido) { this.pedido = pedido; }

    public BigDecimal getSubtotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
