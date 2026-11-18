package br.edu.uniesp.sistemadegestao.internal.seguranca.dto;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.PerfilUsuario;
public record UsuarioResponse(Long id, String nome, String celular, PerfilUsuario perfil, Long clienteId) {}
