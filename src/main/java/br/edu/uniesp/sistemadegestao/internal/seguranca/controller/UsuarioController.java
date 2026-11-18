package br.edu.uniesp.sistemadegestao.internal.seguranca.controller;
import br.edu.uniesp.sistemadegestao.internal.seguranca.dto.*;
import br.edu.uniesp.sistemadegestao.internal.seguranca.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarios;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody CriarUsuarioRequest request) { return usuarios.criar(request); }
}
