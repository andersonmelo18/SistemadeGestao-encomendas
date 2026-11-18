package br.edu.uniesp.sistemadegestao.internal.pedido.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ItemPedidoRequest(
        @NotBlank @Size(max = 150) String produto,
        @NotNull @Min(1) @Max(9999) Integer quantidade,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal valorUnitario) {}
