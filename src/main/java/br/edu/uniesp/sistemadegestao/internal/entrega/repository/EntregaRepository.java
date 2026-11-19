
package br.edu.uniesp.sistemadegestao.internal.entrega.repository;

import br.edu.uniesp.sistemadegestao.internal.entrega.entity.EntregaEntity;
import br.edu.uniesp.sistemadegestao.internal.entrega.entity.StatusEntrega;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntregaRepository extends JpaRepository<EntregaEntity, Long> {

    Page<EntregaEntity> findByStatus(
            StatusEntrega status,
            Pageable pageable
    );

    Page<EntregaEntity> findByEntregadorId(
            Long entregadorId,
            Pageable pageable
    );

    Page<EntregaEntity> findByEntregadorIdAndStatus(
            Long entregadorId,
            StatusEntrega status,
            Pageable pageable
    );

    Optional<EntregaEntity> findByPedidoId(Long pedidoId);
}
