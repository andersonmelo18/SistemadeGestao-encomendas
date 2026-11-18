package br.edu.uniesp.sistemadegestao.internal.pedido.service;

import br.edu.uniesp.sistemadegestao.internal.cliente.service.ClienteService;
import br.edu.uniesp.sistemadegestao.internal.pedido.dto.*;
import br.edu.uniesp.sistemadegestao.internal.pedido.entity.*;
import br.edu.uniesp.sistemadegestao.internal.pedido.mapper.PedidoMapper;
import br.edu.uniesp.sistemadegestao.internal.pedido.repository.PedidoRepository;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.UsuarioEntity;
import br.edu.uniesp.sistemadegestao.internal.seguranca.repository.UsuarioRepository;
import br.edu.uniesp.sistemadegestao.shared.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidoService {
    private final PedidoRepository pedidos;
    private final ClienteService clientes;
    private final UsuarioRepository usuarios;

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public PedidoResponse criar(CriarPedidoRequest request) {
        var cliente = clientes.buscarEntidadePorId(request.clienteId());
        var pedido = new PedidoEntity(cliente, PedidoMapper.paraItens(request.itens()),
                request.observacoes(), usuarioAtual());
        return PedidoMapper.paraResposta(pedidos.saveAndFlush(pedido));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public PedidoResponse atualizar(Long id, AtualizarPedidoRequest request) {
        var pedido = buscarEntidade(id);
        conferirVersao(pedido, request.versao());
        pedido.editar(PedidoMapper.paraItens(request.itens()), request.observacoes());
        pedidos.flush();
        return PedidoMapper.paraResposta(pedido);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCAO')")
    public PedidoResponse alterarStatus(Long id, AlterarStatusPedidoRequest request) {
        var pedido = buscarEntidade(id);
        conferirVersao(pedido, request.versao());
        pedido.transicionar(request.status(), usuarioAtual());
        pedidos.flush();
        return PedidoMapper.paraResposta(pedido);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'PRODUCAO')")
    public PedidoResponse buscar(Long id) {
        return PedidoMapper.paraResposta(buscarEntidade(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'PRODUCAO')")
    public Page<PedidoResponse> listar(StatusPedido status, Pageable pageable) {
        return (status == null ? pedidos.findAll(pageable) : pedidos.findByStatus(status, pageable))
                .map(PedidoMapper::paraResposta);
    }

    @PreAuthorize("hasRole('CLIENTE')")
    public Page<PedidoResponse> listarMeus(StatusPedido status, Pageable pageable) {
        Long clienteId = clienteAtual();
        return (status == null ? pedidos.findByClienteId(clienteId, pageable) :
                pedidos.findByClienteIdAndStatus(clienteId, status, pageable)).map(PedidoMapper::paraResposta);
    }

    @PreAuthorize("hasRole('CLIENTE')")
    public PedidoResponse buscarMeu(Long id) {
        return PedidoMapper.paraResposta(pedidos.findByIdAndClienteId(id, clienteAtual())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id)));
    }

    private PedidoEntity buscarEntidade(Long id) {
        return pedidos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    private void conferirVersao(PedidoEntity pedido, Long versao) {
        if (!Objects.equals(pedido.getVersao(), versao)) throw new ConflitoVersaoException();
    }

    private UsuarioEntity usuarioAtual() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new AccessDeniedException("Acesso negado.");
        var usuario = usuarios.findByCelular(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Acesso negado."));
        if (!usuario.isAtivo()) throw new AccessDeniedException("Acesso negado.");
        return usuario;
    }

    private Long clienteAtual() {
        var cliente = usuarioAtual().getCliente();
        if (cliente == null || !cliente.isAtivo()) throw new AccessDeniedException("Acesso negado.");
        return cliente.getId();
    }
}
