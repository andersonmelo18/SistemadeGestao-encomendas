package br.edu.uniesp.sistemadegestao.internal.seguranca.service;
import br.edu.uniesp.sistemadegestao.internal.seguranca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class AutenticacaoService implements UserDetailsService {
    private final UsuarioRepository usuarios;
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String celular) {
        var usuario = usuarios.findByCelular(celular)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais invalidas"));
        boolean ativo = usuario.isAtivo() &&
                (usuario.getCliente() == null || usuario.getCliente().isAtivo());
        return User.withUsername(usuario.getCelular()).password(usuario.getSenha())
                .roles(usuario.getPerfil().name()).disabled(!ativo).build();
    }
}
