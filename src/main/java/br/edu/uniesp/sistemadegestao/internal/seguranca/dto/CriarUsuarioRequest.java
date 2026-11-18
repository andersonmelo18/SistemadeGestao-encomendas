package br.edu.uniesp.sistemadegestao.internal.seguranca.dto;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.PerfilUsuario;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
public record CriarUsuarioRequest(
        @NotBlank @Size(max=150) String nome,
        @NotBlank @Pattern(regexp="[1-9][0-9]{9,10}") String celular,
        @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) @NotBlank @Size(min=8, max=72) String senha,
        @NotNull PerfilUsuario perfil) {}
