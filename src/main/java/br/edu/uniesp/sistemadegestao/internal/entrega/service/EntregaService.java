
package br.edu.uniesp.sistemadegestao.internal.entrega.service;

import br.edu.uniesp.sistemadegestao.internal.entrega.entity.EntregaEntity;
import br.edu.uniesp.sistemadegestao.internal.entrega.entity.StatusEntrega;
import br.edu.uniesp.sistemadegestao.internal.entrega.repository.EntregaRepository;
import br.edu.uniesp.sistemadegestao.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.sistemadegestao.internal.pedido.entity.StatusPedido;
import br.edu.uniesp.sistemadegestao.internal.pedido.repository.PedidoRepository;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.PerfilUsuario;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.UsuarioEntity;
import br.edu.uniesp.sistemadegestao.internal.seguranca.repository.UsuarioRepository;
import br.edu.uniesp.sistemadegestao.shared.exception.ConflitoVersaoException;
import br.edu.uniesp.sistemadegestao.shared.exception.RecursoNaoEncontradoException;
import br.edu.uniesp.sistemadegestao.shared.exception.RegraNegocioException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntregaService {

    private final EntregaRepository entregas;
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public EntregaEntity criar(
            Long pedidoId,
            Long entregadorId,
            String enderecoEntrega,
            String observacoes
    ) {
        PedidoEntity pedido = pedidos.findById(pedidoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Pedido", pedidoId));

        if (pedido.getStatus() != StatusPedido.FINALIZADO) {
            throw new RegraNegocioException(
                    "Somente pedidos finalizados podem ser encaminhados para entrega."
            );
        }

        if (entregas.findByPedidoId(pedidoId).isPresent()) {
            throw new RegraNegocioException(
                    "Este pedido ja possui uma entrega cadastrada."
            );
        }

        UsuarioEntity entregador = usuarios.findById(entregadorId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Entregador", entregadorId));

        if (!entregador.isAtivo()
                || entregador.getPerfil() != PerfilUsuario.ENTREGA) {
            throw new RegraNegocioException(
                    "O usuario informado nao e um entregador ativo."
            );
        }

        EntregaEntity entrega = new EntregaEntity(
                pedido,
                entregador,
                enderecoEntrega,
                observacoes
        );

        return entregas.save(entrega);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public Page<EntregaEntity> listarTodas(
            StatusEntrega status,
            Pageable pageable
    ) {
        if (status == null) {
            return entregas.findAll(pageable);
        }

        return entregas.findByStatus(status, pageable);
    }

    @PreAuthorize("hasRole('ENTREGA')")
    public Page<EntregaEntity> listarMinhas(
            StatusEntrega status,
            Pageable pageable
    ) {
        Long entregadorId = usuarioAtual().getId();

        if (status == null) {
            return entregas.findByEntregadorId(entregadorId, pageable);
        }

        return entregas.findByEntregadorIdAndStatus(
                entregadorId,
                status,
                pageable
        );
    }

    @Transactional
    @PreAuthorize("hasRole('ENTREGA')")
    public EntregaEntity iniciar(Long id, Long versao) {
        EntregaEntity entrega = buscarEntrega(id);
        conferirResponsavel(entrega);
        conferirVersao(entrega, versao);

        entrega.iniciarEntrega();

        return entregas.save(entrega);
    }

    @Transactional
    @PreAuthorize("hasRole('ENTREGA')")
    public EntregaEntity concluir(Long id, Long versao) {
        EntregaEntity entrega = buscarEntrega(id);
        conferirResponsavel(entrega);
        conferirVersao(entrega, versao);

        entrega.concluirEntrega();

        return entregas.save(entrega);
    }

    private EntregaEntity buscarEntrega(Long id) {
        return entregas.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Entrega", id));
    }

    private void conferirVersao(EntregaEntity entrega, Long versao) {
        if (versao == null
                || !Objects.equals(entrega.getVersao(), versao)) {
            throw new ConflitoVersaoException();
        }
    }

    private void conferirResponsavel(EntregaEntity entrega) {
        if (!Objects.equals(
                entrega.getEntregador().getId(),
                usuarioAtual().getId()
        )) {
            throw new AccessDeniedException(
                    "Voce nao e o responsavel por esta entrega."
            );
        }
    }

    private UsuarioEntity usuarioAtual() {
        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null) {
            throw new AccessDeniedException("Acesso negado.");
        }

        UsuarioEntity usuario = usuarios.findByCelular(authentication.getName())
                .orElseThrow(() ->
                        new AccessDeniedException("Acesso negado."));

        if (!usuario.isAtivo()) {
            throw new AccessDeniedException("Acesso negado.");
        }

        return usuario;
    }
}
