package br.edu.uniesp.sistemadegestao.internal.cliente.service;

import br.edu.uniesp.sistemadegestao.internal.cliente.dto.AtualizarClienteRequest;
import br.edu.uniesp.sistemadegestao.internal.cliente.dto.ClienteResumoResponse;
import br.edu.uniesp.sistemadegestao.internal.cliente.dto.CriarClienteRequest;
import br.edu.uniesp.sistemadegestao.internal.cliente.dto.CriarClienteResponse;
import br.edu.uniesp.sistemadegestao.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.sistemadegestao.internal.cliente.mapper.ClienteMapper;
import br.edu.uniesp.sistemadegestao.internal.cliente.repository.ClienteRepository;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.PerfilUsuario;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.UsuarioEntity;
import br.edu.uniesp.sistemadegestao.internal.seguranca.repository.UsuarioRepository;
import br.edu.uniesp.sistemadegestao.internal.seguranca.service.UsuarioService;
import br.edu.uniesp.sistemadegestao.shared.exception.RegraNegocioException;
import br.edu.uniesp.sistemadegestao.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public CriarClienteResponse criarCliente(CriarClienteRequest request) {
        if (usuarioRepository.existsByCelular(request.celular()))
            throw new RegraNegocioException("Celular ja cadastrado.");
        String senhaHash = usuarioService.codificarSenha(request.senha());
        ClienteEntity cliente = ClienteMapper.converteParaEntidade(request);
        clienteRepository.save(cliente);
        usuarioRepository.save(UsuarioEntity.criarAcesso(
                cliente.getNome(), cliente.getCelular(), senhaHash,
                PerfilUsuario.CLIENTE, cliente));
        return ClienteMapper.converterParaResposta(cliente);
    }

    public ClienteResumoResponse buscarClientePorId(Long id) {
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        return ClienteMapper.paraResumoResposta(cliente);
    }
    
    @Transactional(readOnly = true)
    public Page<ClienteResumoResponse> listarTodos(Pageable pageable) {
        return clienteRepository.findAll(pageable)
                .map(ClienteMapper::paraResumoResposta);
    }

    @Transactional
    public ClienteResumoResponse atualizarCliente(Long id, AtualizarClienteRequest request) {
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        cliente.alterarNome(request.nome());
        usuarioRepository.findByClienteId(id).ifPresent(usuario -> usuario.alterarNome(request.nome()));
        cliente.alterarEmail(request.email());
        clienteRepository.save(cliente);
        return ClienteMapper.paraResumoResposta(cliente);
    }

    @Transactional
    public void inativarCliente(Long id) {
        ClienteEntity cliente = clienteRepository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Cliente", id));
        cliente.inativar();
    }

    @Transactional
    public void ativarCliente(Long id) {
        ClienteEntity cliente = clienteRepository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Cliente", id));
        cliente.ativar();
    }

    @Transactional(readOnly = true)
    public ClienteEntity buscarEntidadePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Cliente", id)
                );
    }

}
