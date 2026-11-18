package br.edu.uniesp.sistemadegestao.internal.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.br.CPF;

public record CriarClienteRequest(
        @NotBlank @Size(max=150) String nome,
         @CPF String cpf,
         @Email @Size(max=150) String email,
         @NotBlank @Pattern(regexp="[1-9][0-9]{9,10}") String celular,
         @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) @NotBlank @Size(min=8, max=72) String senha) {
}
