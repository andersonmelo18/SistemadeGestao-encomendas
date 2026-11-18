package br.edu.uniesp.sistemadegestao.internal.pedido.controller;

import br.edu.uniesp.sistemadegestao.internal.pedido.dto.PedidoResponse;
import br.edu.uniesp.sistemadegestao.internal.pedido.entity.StatusPedido;
import br.edu.uniesp.sistemadegestao.internal.pedido.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meus-pedidos")
@RequiredArgsConstructor
public class MeusPedidosController {
    private final PedidoService pedidos;
    @GetMapping
    public Page<PedidoResponse> listar(@RequestParam(required = false) StatusPedido status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return pedidos.listarMeus(status, pageable);
    }
    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id) { return pedidos.buscarMeu(id); }
}
