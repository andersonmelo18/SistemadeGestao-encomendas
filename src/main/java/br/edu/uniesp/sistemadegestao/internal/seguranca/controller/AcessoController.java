package br.edu.uniesp.sistemadegestao.internal.seguranca.controller;
import br.edu.uniesp.sistemadegestao.internal.seguranca.dto.UsuarioResponse;
import br.edu.uniesp.sistemadegestao.internal.seguranca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
@RestController
@RequestMapping("/api/acesso")
@RequiredArgsConstructor
public class AcessoController {
    private final UsuarioService usuarios;
    @GetMapping("/me")
    public UsuarioResponse atual(Principal principal) { return usuarios.buscarAtual(principal.getName()); }
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) { return token; }
}
