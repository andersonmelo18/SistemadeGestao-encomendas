package br.edu.uniesp.sistemadegestao.internal.pedido.controller;

import br.edu.uniesp.sistemadegestao.internal.pedido.dto.*;
import br.edu.uniesp.sistemadegestao.internal.pedido.entity.StatusPedido;
import br.edu.uniesp.sistemadegestao.internal.pedido.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService pedidos;
    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody CriarPedidoRequest request) {
        var response = pedidos.criar(request);
        return ResponseEntity.created(URI.create("/api/pedidos/" + response.id())).body(response);
    }
    @GetMapping
    public Page<PedidoResponse> listar(@RequestParam(required = false) StatusPedido status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return pedidos.listar(status, pageable);
    }
    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id) { return pedidos.buscar(id); }
    @PutMapping("/{id}")
    public PedidoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarPedidoRequest request) {
        return pedidos.atualizar(id, request);
    }
    @PatchMapping("/{id}/status")
    public PedidoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody AlterarStatusPedidoRequest request) {
        return pedidos.alterarStatus(id, request);
    }
}
