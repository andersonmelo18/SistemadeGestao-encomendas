package br.edu.uniesp.sistemadegestao.internal.ordemservico.mapper;

import br.edu.uniesp.sistemadegestao.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.sistemadegestao.internal.equipamento.entity.EquipamentoEntity;
import br.edu.uniesp.sistemadegestao.internal.ordemservico.dto.CriarOrdemServicoRequest;
import br.edu.uniesp.sistemadegestao.internal.ordemservico.dto.CriarOrdemServicoResponse;
import br.edu.uniesp.sistemadegestao.internal.ordemservico.entity.OrdemServicoEntity;

public final class OrdemServicoMapper {

    private OrdemServicoMapper() {
    }

    public static OrdemServicoEntity paraEntidade(CriarOrdemServicoRequest request,
                                                  String numeroProtocolo,
                                                  ClienteEntity cliente,
                                                  EquipamentoEntity equipamento) {
        return new OrdemServicoEntity(
                numeroProtocolo,
                cliente,
                equipamento,
                request.descricaoProblema()
        );
    }

    public static CriarOrdemServicoResponse paraResposta(OrdemServicoEntity os) {
        return new CriarOrdemServicoResponse(
                os.getId(),
                os.getNumeroProtocolo(),
                os.getStatus(),
                os.getDataAbertura()
        );
    }
}
