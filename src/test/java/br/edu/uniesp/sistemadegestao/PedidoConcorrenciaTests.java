package br.edu.uniesp.sistemadegestao;

import br.edu.uniesp.sistemadegestao.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.sistemadegestao.internal.pedido.entity.*;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PedidoConcorrenciaTests {
    @Autowired EntityManagerFactory factory;

    @Test void entradaEmProducaoImpedeEdicaoConcorrente() { verificarConcorrencia(true); }
    @Test void edicaoConcorrenteExigeQueProducaoReleiaPedido() { verificarConcorrencia(false); }

    private void verificarConcorrencia(boolean producaoPrimeiro) {
        Long pedidoId;
        Long clienteId;
        Long usuarioId;
        try (var em = factory.createEntityManager()) {
            em.getTransaction().begin();
            var cliente = new ClienteEntity("Cliente concorrencia", null, null);
            var usuario = UsuarioEntity.criarAcesso("Producao", null, "hash", PerfilUsuario.PRODUCAO, null);
            em.persist(cliente);
            em.persist(usuario);
            var pedido = new PedidoEntity(cliente,
                    List.of(new ItemPedidoEntity("Original", 1, new BigDecimal("20.00"))), null, usuario);
            pedido.transicionar(StatusPedido.APROVADO, usuario);
            pedido.transicionar(StatusPedido.FILA, usuario);
            em.persist(pedido);
            em.getTransaction().commit();
            pedidoId = pedido.getId();
            clienteId = cliente.getId();
            usuarioId = usuario.getId();
        }
        try (var primeira = factory.createEntityManager(); var segunda = factory.createEntityManager()) {
            primeira.getTransaction().begin();
            segunda.getTransaction().begin();
            var pedido1 = primeira.find(PedidoEntity.class, pedidoId);
            var pedido2 = segunda.find(PedidoEntity.class, pedidoId);
            // Materializar ambas as leituras antes do commit concorrente.
            pedido1.getItens().size();
            pedido2.getItens().size();
            if (producaoPrimeiro) {
                pedido1.transicionar(StatusPedido.EM_PRODUCAO, primeira.find(UsuarioEntity.class, usuarioId));
            } else {
                pedido1.editar(List.of(new ItemPedidoEntity("Revisado", 2, new BigDecimal("30.00"))), "Edicao");
            }
            primeira.getTransaction().commit();
            if (producaoPrimeiro) {
                pedido2.editar(List.of(new ItemPedidoEntity("Revisado", 2, new BigDecimal("30.00"))), "Edicao");
            } else {
                pedido2.transicionar(StatusPedido.EM_PRODUCAO, segunda.find(UsuarioEntity.class, usuarioId));
            }
            var erro = assertThrows(RollbackException.class, () -> segunda.getTransaction().commit());
            assertInstanceOf(OptimisticLockException.class, erro.getCause());
            try (var leitura = factory.createEntityManager()) {
                var persistido = leitura.find(PedidoEntity.class, pedidoId);
                assertEquals(producaoPrimeiro ? StatusPedido.EM_PRODUCAO : StatusPedido.FILA, persistido.getStatus());
                assertEquals(producaoPrimeiro ? "Original" : "Revisado", persistido.getItens().getFirst().getProduto());
                assertEquals(new BigDecimal(producaoPrimeiro ? "20.00" : "60.00"), persistido.getValorTotal());
                assertEquals(producaoPrimeiro ? 4 : 3, persistido.getHistorico().size());
            }
        } finally {
            try (var em = factory.createEntityManager()) {
                em.getTransaction().begin();
                em.remove(em.find(PedidoEntity.class, pedidoId));
                em.remove(em.find(UsuarioEntity.class, usuarioId));
                em.remove(em.find(ClienteEntity.class, clienteId));
                em.getTransaction().commit();
            }
        }
    }
}
