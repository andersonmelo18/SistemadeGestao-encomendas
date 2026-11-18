package br.edu.uniesp.sistemadegestao.config;
import br.edu.uniesp.sistemadegestao.internal.seguranca.dto.CriarUsuarioRequest;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.PerfilUsuario;
import br.edu.uniesp.sistemadegestao.internal.seguranca.repository.UsuarioRepository;
import br.edu.uniesp.sistemadegestao.internal.seguranca.service.UsuarioService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
@Configuration
public class AdministradorInicialConfig {
    @Bean
    ApplicationRunner administradorInicial(Environment env, UsuarioRepository repository, UsuarioService service) {
        return args -> {
            String celular = env.getProperty("app.admin.celular");
            String senha = env.getProperty("app.admin.senha");
            if (celular == null && senha == null) return;
            if (celular == null || !celular.matches("[1-9][0-9]{9,10}") || senha == null)
                throw new IllegalStateException("Configure APP_ADMIN_CELULAR e APP_ADMIN_SENHA validos.");
            if (repository.count() == 0)
                service.criar(new CriarUsuarioRequest("Administrador", celular, senha, PerfilUsuario.ADMIN));
        };
    }
}
