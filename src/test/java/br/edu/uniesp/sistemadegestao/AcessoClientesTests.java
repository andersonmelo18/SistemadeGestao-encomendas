package br.edu.uniesp.sistemadegestao;

import br.edu.uniesp.sistemadegestao.internal.cliente.dto.*;
import br.edu.uniesp.sistemadegestao.internal.cliente.service.ClienteService;
import br.edu.uniesp.sistemadegestao.internal.seguranca.dto.CriarUsuarioRequest;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.PerfilUsuario;
import br.edu.uniesp.sistemadegestao.internal.seguranca.repository.UsuarioRepository;
import br.edu.uniesp.sistemadegestao.internal.seguranca.service.UsuarioService;
import br.edu.uniesp.sistemadegestao.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AcessoClientesTests {
    @Autowired WebApplicationContext context;
    @Autowired ClienteService clientes;
    @Autowired UsuarioService usuarios;
    @Autowired UsuarioRepository repository;
    @Autowired PasswordEncoder encoder;
    MockMvc mvc;
    private static final String CELULAR = "83999990001";
    private static final String SENHA = "SenhaTeste123";

    @BeforeEach void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
    CriarClienteResponse cliente() {
        return clientes.criarCliente(new CriarClienteRequest("Maria", null, null, CELULAR, SENHA));
    }
    @Test void cadastroCriaAcessoVinculadoComSenhaProtegida() throws Exception {
        var cliente = cliente();
        var usuario = repository.findByCelular(CELULAR).orElseThrow();
        assertEquals(cliente.id(), usuario.getCliente().getId());
        assertNotEquals(SENHA, usuario.getSenha());
        assertTrue(encoder.matches(SENHA, usuario.getSenha()));
        mvc.perform(get("/api/acesso/me").with(httpBasic(CELULAR, SENHA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.clienteId").value(cliente.id()))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }
    @Test void clienteNaoAcessaListaDeClientes() throws Exception {
        cliente();
        mvc.perform(get("/clientes").with(httpBasic(CELULAR, SENHA))).andExpect(status().isForbidden());
    }
    @Test void anonimoESenhaIncorretaNaoAutenticam() throws Exception {
        cliente();
        mvc.perform(get("/api/acesso/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/acesso/me").with(httpBasic(CELULAR, "errada"))).andExpect(status().isUnauthorized());
    }
    @Test void clienteInativoPerdeAcessoEReativacaoRestaura() throws Exception {
        var cliente = cliente();
        clientes.inativarCliente(cliente.id());
        mvc.perform(get("/api/acesso/me").with(httpBasic(CELULAR, SENHA))).andExpect(status().isUnauthorized());
        clientes.ativarCliente(cliente.id());
        mvc.perform(get("/api/acesso/me").with(httpBasic(CELULAR, SENHA))).andExpect(status().isOk());
    }
    @Test void celularDuplicadoNaoCriaOutroUsuario() {
        cliente();
        assertThrows(RegraNegocioException.class, this::cliente);
        assertEquals(1, repository.count());
    }
    @Test void atendenteCadastraClienteSemCpfOuEmail() throws Exception {
        mvc.perform(post("/clientes").with(user("operador").roles("ATENDENTE")).with(csrf())
                .contentType("application/json")
                .content("{\"nome\":\"Maria\",\"celular\":\"83999990001\",\"senha\":\"SenhaTeste123\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }
    @Test void cadastroRejeitaDadosInvalidosECsrfAusente() throws Exception {
        mvc.perform(post("/clientes").with(user("operador").roles("ATENDENTE"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/clientes").with(user("operador").roles("ATENDENTE")).with(csrf())
                .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        assertEquals(0, repository.count());
    }
    @Test void somenteAdminCriaUsuariosInternos() throws Exception {
        String body = "{\"nome\":\"Producao\",\"celular\":\"83999990002\",\"senha\":\"SenhaTeste123\",\"perfil\":\"PRODUCAO\"}";
        mvc.perform(post("/api/usuarios").with(user("operador").roles("ATENDENTE")).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/usuarios").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isCreated());
        mvc.perform(get("/clientes").with(httpBasic("83999990002", SENHA))).andExpect(status().isForbidden());
    }
    @Test void usuarioInativoNaoAutentica() throws Exception {
        usuarios.criar(new CriarUsuarioRequest("Entrega", CELULAR, SENHA, PerfilUsuario.ENTREGA));
        repository.findByCelular(CELULAR).orElseThrow().inativar();
        mvc.perform(get("/api/acesso/me").with(httpBasic(CELULAR, SENHA))).andExpect(status().isUnauthorized());
    }
    @Test void respostaPreservaCpfEEmailNosCamposCorretos() {
        var cliente = clientes.criarCliente(new CriarClienteRequest("Maria", "52998224725", "maria@example.com", CELULAR, SENHA));
        var resposta = clientes.buscarClientePorId(cliente.id());
        assertEquals("52998224725", resposta.cpf());
        assertEquals("maria@example.com", resposta.email());
        clientes.atualizarCliente(cliente.id(), new AtualizarClienteRequest("Maria Silva", null));
        assertEquals("Maria Silva", usuarios.buscarAtual(CELULAR).nome());
    }
}
