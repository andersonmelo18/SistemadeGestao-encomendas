package br.edu.uniesp.sistemadegestao.internal.ordemservico.controller;

import br.edu.uniesp.sistemadegestao.internal.ordemservico.dto.CriarOrdemServicoRequest;
import br.edu.uniesp.sistemadegestao.internal.ordemservico.dto.CriarOrdemServicoResponse;
import br.edu.uniesp.sistemadegestao.internal.ordemservico.service.OrdemServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ordens")
@RequiredArgsConstructor
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    @PostMapping
    public ResponseEntity<CriarOrdemServicoResponse > abrir (
            @Valid @RequestBody CriarOrdemServicoRequest criarOrdemServicoRequest){
        CriarOrdemServicoResponse response = ordemServicoService
                .abrirOrdemServico(criarOrdemServicoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
