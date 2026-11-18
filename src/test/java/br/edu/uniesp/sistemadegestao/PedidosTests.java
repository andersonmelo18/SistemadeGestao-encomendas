package br.edu.uniesp.sistemadegestao;

import br.edu.uniesp.sistemadegestao.internal.cliente.dto.CriarClienteRequest;
import br.edu.uniesp.sistemadegestao.internal.cliente.service.ClienteService;
import br.edu.uniesp.sistemadegestao.internal.pedido.dto.*;
import br.edu.uniesp.sistemadegestao.internal.pedido.entity.StatusPedido;
import br.edu.uniesp.sistemadegestao.internal.pedido.repository.PedidoRepository;
import br.edu.uniesp.sistemadegestao.internal.pedido.service.PedidoService;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.*;
import br.edu.uniesp.sistemadegestao.internal.seguranca.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@WithMockUser(username = "83999990009", roles = "ADMIN")
class PedidosTests {
    @Autowired WebApplicationContext context;
    @Autowired ClienteService clientes;
    @Autowired PedidoService pedidos;
    @Autowired PedidoRepository repository;
    @Autowired UsuarioRepository usuarios;
    MockMvc mvc;
    Long clienteId;
    static final String OPERADOR = "83999990009";
    static final String CELULAR = "83999990001";

    @BeforeEach void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        usuarios.save(UsuarioEntity.criarAcesso("Operador", OPERADOR, "hash-nao-usado", PerfilUsuario.ADMIN, null));
        clienteId = clientes.criarCliente(new CriarClienteRequest("Maria", null, null, CELULAR, "SenhaTeste123")).id();
    }
    List<ItemPedidoRequest> itens() {
        return List.of(new ItemPedidoRequest("Caneca", 3, new BigDecimal("19.90")),
                new ItemPedidoRequest("Camisa", 2, new BigDecimal("25.50")));
    }
    PedidoResponse criar() {
        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
        return pedidos.criar(new CriarPedidoRequest(clienteId, itens(), "Personalizado"));
    }
    PedidoResponse avancar(PedidoResponse pedido, StatusPedido status) {
        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
        return pedidos.alterarStatus(pedido.id(), new AlterarStatusPedidoRequest(pedido.versao(), status));
    }
    PedidoResponse buscar(Long id) {
        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
        return pedidos.buscar(id);
    }
    PedidoResponse fila() {
        return avancar(avancar(criar(), StatusPedido.APROVADO), StatusPedido.FILA);
    }
    String corpoEdicao(Long versao) {
        return "{\"versao\":" + versao +
                ",\"itens\":[{\"produto\":\"Caneca alterada\",\"quantidade\":1,\"valorUnitario\":10.25}],\"observacoes\":\"Nova arte\"}";
    }

    @Test void criaPedidoComTotalCalculadoEHistorico() throws Exception {
        mvc.perform(post("/api/pedidos").with(user(OPERADOR).roles("ATENDENTE")).with(csrf())
                .contentType("application/json").content("{\"clienteId\":" + clienteId +
                        ",\"itens\":[{\"produto\":\"Caneca\",\"quantidade\":3,\"valorUnitario\":19.90}," +
                        "{\"produto\":\"Camisa\",\"quantidade\":2,\"valorUnitario\":25.50}]}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.valorTotal").value(110.70))
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.versao").value(0))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.historico[0].responsavel").value("Operador"));
    }

    @Test void fluxoInteiroAvancaJuntoEMantemHistorico() {
        var pedido = criar();
        for (var status : List.of(StatusPedido.APROVADO, StatusPedido.FILA, StatusPedido.EM_PRODUCAO, StatusPedido.FINALIZADO)) {
            long versaoAnterior = pedido.versao();
            pedido = avancar(pedido, status);
            assertEquals(versaoAnterior + 1, pedido.versao());
            assertEquals(2, pedido.itens().size());
        }
        assertFalse(pedido.permiteEdicao());
        assertEquals(5, pedido.historico().size());
        assertEquals(StatusPedido.EM_PRODUCAO, pedido.historico().get(4).statusAnterior());
        assertEquals(StatusPedido.FINALIZADO, pedido.historico().get(4).novoStatus());
    }

    @Test void permiteEdicaoAteFilaInclusive() throws Exception {
        var pedido = criar();
        for (var etapa : List.of(StatusPedido.EM_ANALISE, StatusPedido.APROVADO, StatusPedido.FILA)) {
            if (etapa != StatusPedido.EM_ANALISE) pedido = avancar(pedido, etapa);
            mvc.perform(put("/api/pedidos/" + pedido.id()).with(user(OPERADOR).roles("ATENDENTE")).with(csrf())
                    .contentType("application/json").content(corpoEdicao(pedido.versao())))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.valorTotal").value(10.25))
                    .andExpect(jsonPath("$.itens.length()").value(1))
                    .andExpect(jsonPath("$.permiteEdicao").value(true));
            pedido = buscar(pedido.id());
        }
        assertEquals(3, pedido.historico().size());
    }

    @Test void bloqueiaEdicaoEmProducaoEFinalizadoInclusiveAdmin() throws Exception {
        var pedido = avancar(fila(), StatusPedido.EM_PRODUCAO);
        for (var etapa : List.of(StatusPedido.EM_PRODUCAO, StatusPedido.FINALIZADO)) {
            if (etapa == StatusPedido.FINALIZADO) pedido = avancar(pedido, etapa);
            mvc.perform(put("/api/pedidos/" + pedido.id()).with(csrf())
                    .contentType("application/json").content(corpoEdicao(pedido.versao())))
                    .andExpect(status().isUnprocessableContent());
            assertEquals(new BigDecimal("110.70"), buscar(pedido.id()).valorTotal());
        }
    }

    @Test void rejeitaPularRepetirEVoltarEtapas() throws Exception {
        var pedido = criar();
        for (var destino : List.of("EM_PRODUCAO", "EM_ANALISE", "FINALIZADO")) {
            mvc.perform(patch("/api/pedidos/" + pedido.id() + "/status").with(csrf())
                    .contentType("application/json")
                    .content("{\"versao\":0,\"status\":\"" + destino + "\"}"))
                    .andExpect(status().isUnprocessableContent());
        }
        pedido = avancar(pedido, StatusPedido.APROVADO);
        mvc.perform(patch("/api/pedidos/" + pedido.id() + "/status").with(csrf())
                .contentType("application/json").content("{\"versao\":1,\"status\":\"EM_ANALISE\"}"))
                .andExpect(status().isUnprocessableContent());
        assertEquals(2, buscar(pedido.id()).historico().size());
    }

    @Test void versaoAntigaRejeitaEdicaoETransicao() throws Exception {
        var pedido = criar();
        avancar(pedido, StatusPedido.APROVADO);
        mvc.perform(put("/api/pedidos/" + pedido.id()).with(csrf())
                .contentType("application/json").content(corpoEdicao(pedido.versao())))
                .andExpect(status().isConflict());
        mvc.perform(patch("/api/pedidos/" + pedido.id() + "/status").with(csrf())
                .contentType("application/json").content("{\"versao\":0,\"status\":\"FILA\"}"))
                .andExpect(status().isConflict());
    }

    @Test void clienteConsultaSomenteSeusPedidos() throws Exception {
        var proprio = criar();
        var outroCliente = clientes.criarCliente(new CriarClienteRequest("Outra", null, null, "83999990002", "SenhaTeste123"));
        var alheio = pedidos.criar(new CriarPedidoRequest(outroCliente.id(), itens(), null));
        mvc.perform(get("/api/meus-pedidos").with(user(CELULAR).roles("CLIENTE")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(proprio.id()));
        mvc.perform(get("/api/meus-pedidos/" + proprio.id()).with(user(CELULAR).roles("CLIENTE")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/meus-pedidos/" + alheio.id()).with(user(CELULAR).roles("CLIENTE")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/pedidos").with(user(CELULAR).roles("CLIENTE")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/pedidos/" + proprio.id()).with(user(CELULAR).roles("CLIENTE")).with(csrf())
                .contentType("application/json").content(corpoEdicao(proprio.versao())))
                .andExpect(status().isForbidden());
    }

    @Test void separaPermissoesAtendimentoProducaoEEntrega() throws Exception {
        var pedido = criar();
        mvc.perform(patch("/api/pedidos/" + pedido.id() + "/status").with(user(OPERADOR).roles("ATENDENTE")).with(csrf())
                .contentType("application/json").content("{\"versao\":0,\"status\":\"APROVADO\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/pedidos/" + pedido.id()).with(user(OPERADOR).roles("PRODUCAO")).with(csrf())
                .contentType("application/json").content(corpoEdicao(0L))).andExpect(status().isForbidden());
        mvc.perform(patch("/api/pedidos/" + pedido.id() + "/status").with(user(OPERADOR).roles("PRODUCAO")).with(csrf())
                .contentType("application/json").content("{\"versao\":0,\"status\":\"APROVADO\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/pedidos").with(user(OPERADOR).roles("ENTREGA"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/pedidos").with(anonymous())).andExpect(status().isUnauthorized());
    }

    @Test void filtraStatusEPaginaResultados() throws Exception {
        avancar(criar(), StatusPedido.APROVADO);
        criar();
        mvc.perform(get("/api/pedidos?status=APROVADO&size=1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("APROVADO"));
        mvc.perform(get("/api/meus-pedidos?status=FILA").with(user(CELULAR).roles("CLIENTE")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test void validaItensValoresEVersao() throws Exception {
        for (String itens : List.of("[]", "[null]",
                "[{\"produto\":\"X\",\"quantidade\":0,\"valorUnitario\":2}]",
                "[{\"produto\":\"X\",\"quantidade\":1,\"valorUnitario\":-2}]",
                "[{\"produto\":\"X\",\"quantidade\":1,\"valorUnitario\":2.001}]",
                "[{\"produto\":\" \",\"quantidade\":1,\"valorUnitario\":2}]")) {
            mvc.perform(post("/api/pedidos").with(csrf()).contentType("application/json")
                    .content("{\"clienteId\":" + clienteId + ",\"itens\":" + itens + "}"))
                    .andExpect(status().isBadRequest());
        }
        assertEquals(0, repository.count());
        var pedido = criar();
        mvc.perform(patch("/api/pedidos/" + pedido.id() + "/status").with(csrf())
                .contentType("application/json").content("{\"status\":\"APROVADO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void rejeitaClienteInativoERecursosInexistentes() throws Exception {
        clientes.inativarCliente(clienteId);
        mvc.perform(post("/api/pedidos").with(csrf()).contentType("application/json")
                .content("{\"clienteId\":" + clienteId +
                        ",\"itens\":[{\"produto\":\"X\",\"quantidade\":1,\"valorUnitario\":2}]}"))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(get("/api/pedidos/999999")).andExpect(status().isNotFound());
        assertEquals(0, repository.count());
    }
}
