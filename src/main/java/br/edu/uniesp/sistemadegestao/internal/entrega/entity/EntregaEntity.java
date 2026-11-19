
package br.edu.uniesp.sistemadegestao.internal.entrega.entity;

import br.edu.uniesp.sistemadegestao.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.UsuarioEntity;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.PerfilUsuario;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "entregas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntregaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long versao;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private PedidoEntity pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entregador_id", nullable = false)
    private UsuarioEntity entregador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEntrega status = StatusEntrega.AGUARDANDO;

    @Column(name = "endereco_entrega", nullable = false, length = 300)
    private String enderecoEntrega;

    @Column(length = 1000)
    private String observacoes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "saiu_em")
    private LocalDateTime saiuEm;

    @Column(name = "entregue_em")
    private LocalDateTime entregueEm;

    public EntregaEntity(
            PedidoEntity pedido,
            UsuarioEntity entregador,
            String enderecoEntrega,
            String observacoes
    ) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido obrigatorio.");
        }

        if (entregador == null ||
                entregador.getPerfil() != PerfilUsuario.ENTREGA) {
            throw new IllegalArgumentException(
                    "O responsavel precisa ter perfil de entrega."
            );
        }

        if (enderecoEntrega == null || enderecoEntrega.isBlank()
                || enderecoEntrega.length() > 300) {
            throw new IllegalArgumentException(
                    "Informe um endereco de entrega valido."
            );
        }

        if (observacoes != null && observacoes.length() > 1000) {
            throw new IllegalArgumentException(
                    "As observacoes devem ter no maximo 1000 caracteres."
            );
        }

        this.pedido = pedido;
        this.entregador = entregador;
        this.enderecoEntrega = enderecoEntrega;
        this.observacoes = observacoes;
        this.status = StatusEntrega.AGUARDANDO;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public void iniciarEntrega() {
        if (status != StatusEntrega.AGUARDANDO) {
            throw new IllegalStateException(
                    "A entrega nao esta aguardando."
            );
        }

        this.status = StatusEntrega.EM_TRANSITO;
        this.saiuEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public void concluirEntrega() {
        if (status != StatusEntrega.EM_TRANSITO) {
            throw new IllegalStateException(
                    "A entrega precisa estar em transito."
            );
        }

        this.status = StatusEntrega.ENTREGUE;
        this.entregueEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }
}
