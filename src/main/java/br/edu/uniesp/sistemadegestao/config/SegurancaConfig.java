package br.edu.uniesp.sistemadegestao.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
@Configuration
@EnableMethodSecurity
public class SegurancaConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/app.js", "/styles.css").permitAll()
                        .requestMatchers("/api/acesso/csrf").permitAll()
                        .requestMatchers("/api/acesso/me").authenticated()
                        .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/clientes", "/clientes/**").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.GET, "/api/meus-pedidos", "/api/meus-pedidos/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/pedidos", "/api/pedidos/*").hasAnyRole("ADMIN", "ATENDENTE", "PRODUCAO")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/status").hasAnyRole("ADMIN", "PRODUCAO")
                        .requestMatchers("/api/pedidos/**", "/api/meus-pedidos/**").denyAll()
                        .anyRequest().hasRole("ADMIN"))
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
                .build();
    }
}
