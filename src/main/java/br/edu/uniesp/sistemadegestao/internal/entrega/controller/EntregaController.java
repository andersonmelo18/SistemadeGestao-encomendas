
package br.edu.uniesp.sistemadegestao.internal.entrega.controller;

import br.edu.uniesp.sistemadegestao.internal.entrega.dto.AlterarStatusEntregaRequest;
import br.edu.uniesp.sistemadegestao.internal.entrega.dto.CriarEntregaRequest;
import br.edu.uniesp.sistemadegestao.internal.entrega.dto.EntregaResponse;
import br.edu.uniesp.sistemadegestao.internal.entrega.entity.StatusEntrega;
import br.edu.uniesp.sistemadegestao.internal.entrega.mapper.EntregaMapper;
import br.edu.uniesp.sistemadegestao.internal.entrega.service.EntregaService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EntregaController {

    private final EntregaService service;

    @PostMapping("/entregas")
    public ResponseEntity<EntregaResponse> criar(
            @RequestBody CriarEntregaRequest request
    ) {
        var entrega = service.criar(
                request.pedidoId(),
                request.entregadorId(),
                request.enderecoEntrega(),
                request.observacoes()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(EntregaMapper.paraResposta(entrega));
    }

    @GetMapping("/entregas")
    public Page<EntregaResponse> listarTodas(
            @RequestParam(required = false) StatusEntrega status,
            Pageable pageable
    ) {
        return service.listarTodas(status, pageable)
                .map(EntregaMapper::paraResposta);
    }

    @GetMapping("/minhas-entregas")
    public Page<EntregaResponse> listarMinhas(
            @RequestParam(required = false) StatusEntrega status,
            Pageable pageable
    ) {
        return service.listarMinhas(status, pageable)
                .map(EntregaMapper::paraResposta);
    }

    @PatchMapping("/entregas/{id}/iniciar")
    public EntregaResponse iniciar(
            @PathVariable Long id,
            @RequestBody AlterarStatusEntregaRequest request
    ) {
        return EntregaMapper.paraResposta(
                service.iniciar(id, request.versao())
        );
    }

    @PatchMapping("/entregas/{id}/concluir")
    public EntregaResponse concluir(
            @PathVariable Long id,
            @RequestBody AlterarStatusEntregaRequest request
    ) {
        return EntregaMapper.paraResposta(
                service.concluir(id, request.versao())
        );
    }
}
