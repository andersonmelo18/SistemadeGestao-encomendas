package br.edu.uniesp.sistemadegestao.internal.pedido.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record AtualizarPedidoRequest(
        @NotNull @PositiveOrZero Long versao,
        @NotEmpty @Size(max = 100) List<@NotNull @Valid ItemPedidoRequest> itens,
        @Size(max = 2000) String observacoes) {}
