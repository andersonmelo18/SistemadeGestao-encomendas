package br.edu.uniesp.sistemadegestao.internal.pedido.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record CriarPedidoRequest(
        @NotNull @Positive Long clienteId,
        @NotEmpty @Size(max = 100) List<@NotNull @Valid ItemPedidoRequest> itens,
        @Size(max = 2000) String observacoes) {}
