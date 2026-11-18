package br.edu.uniesp.sistemadegestao.internal.pedido.dto;
import br.edu.uniesp.sistemadegestao.internal.pedido.entity.StatusPedido;
import jakarta.validation.constraints.*;
public record AlterarStatusPedidoRequest(
        @NotNull @PositiveOrZero Long versao,
        @NotNull StatusPedido status) {}
