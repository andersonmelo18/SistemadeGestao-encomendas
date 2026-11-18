package br.edu.uniesp.sistemadegestao.internal.seguranca.mapper;
import br.edu.uniesp.sistemadegestao.internal.seguranca.dto.UsuarioResponse;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.UsuarioEntity;
public final class UsuarioMapper {
    private UsuarioMapper() {}
    public static UsuarioResponse paraResposta(UsuarioEntity usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getCelular(),
                usuario.getPerfil(), usuario.getCliente() == null ? null : usuario.getCliente().getId());
    }
}
