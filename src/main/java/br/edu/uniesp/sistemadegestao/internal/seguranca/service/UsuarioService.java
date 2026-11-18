package br.edu.uniesp.sistemadegestao.internal.seguranca.service;
import br.edu.uniesp.sistemadegestao.internal.seguranca.dto.*;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.*;
import br.edu.uniesp.sistemadegestao.internal.seguranca.mapper.UsuarioMapper;
import br.edu.uniesp.sistemadegestao.internal.seguranca.repository.UsuarioRepository;
import br.edu.uniesp.sistemadegestao.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    @Transactional
    public UsuarioResponse criar(CriarUsuarioRequest request) {
        if (request.perfil() == PerfilUsuario.CLIENTE || request.perfil() == PerfilUsuario.TECNICO)
            throw new RegraNegocioException("Use o cadastro de clientes para acesso de cliente; tecnico e um perfil legado.");
        if (usuarios.existsByCelular(request.celular()))
            throw new RegraNegocioException("Celular ja cadastrado.");
        return UsuarioMapper.paraResposta(usuarios.save(UsuarioEntity.criarAcesso(
                request.nome(), request.celular(), codificarSenha(request.senha()), request.perfil(), null)));
    }
    public String codificarSenha(String senha) {
        if (senha == null || senha.length() < 8 || senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new RegraNegocioException("A senha deve ter no minimo 8 caracteres e no maximo 72 bytes.");
        return encoder.encode(senha);
    }
    @Transactional(readOnly=true)
    public UsuarioResponse buscarAtual(String celular) {
        return UsuarioMapper.paraResposta(usuarios.findByCelular(celular).orElseThrow());
    }
}
