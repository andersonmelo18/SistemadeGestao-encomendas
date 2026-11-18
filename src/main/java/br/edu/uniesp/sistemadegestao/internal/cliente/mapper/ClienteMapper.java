package br.edu.uniesp.sistemadegestao.internal.cliente.mapper;

import br.edu.uniesp.sistemadegestao.internal.cliente.dto.ClienteResumoResponse;
import br.edu.uniesp.sistemadegestao.internal.cliente.dto.CriarClienteRequest;
import br.edu.uniesp.sistemadegestao.internal.cliente.dto.CriarClienteResponse;
import br.edu.uniesp.sistemadegestao.internal.cliente.entity.ClienteEntity;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static ClienteEntity converteParaEntidade(CriarClienteRequest request) {
        return new ClienteEntity(
                request.nome(),
                request.cpf(),
                request.email(),
                request.celular()
        );
    }
    public static CriarClienteResponse converterParaResposta(ClienteEntity cliente) {
        return new CriarClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getCelular()
        );
    }

    public static ClienteResumoResponse paraResumoResposta(
            ClienteEntity cliente) {
        return new ClienteResumoResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.isAtivo(),
                cliente.getCelular()
        );
    }

}
