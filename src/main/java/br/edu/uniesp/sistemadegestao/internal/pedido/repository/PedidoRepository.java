package br.edu.uniesp.sistemadegestao.internal.pedido.repository;
import br.edu.uniesp.sistemadegestao.internal.pedido.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
    Page<PedidoEntity> findByStatus(StatusPedido status, Pageable pageable);
    Page<PedidoEntity> findByClienteId(Long clienteId, Pageable pageable);
    Page<PedidoEntity> findByClienteIdAndStatus(Long clienteId, StatusPedido status, Pageable pageable);
    Optional<PedidoEntity> findByIdAndClienteId(Long id, Long clienteId);
}
