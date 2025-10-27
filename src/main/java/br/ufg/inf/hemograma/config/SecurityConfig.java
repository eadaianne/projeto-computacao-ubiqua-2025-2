package br.ufg.inf.hemograma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança para a aplicação.
 * Permite acesso livre aos endpoints FHIR e de desenvolvimento.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF para APIs REST
            .csrf(csrf -> csrf.disable())
            
            // Configura autorização de requisições
            .authorizeHttpRequests(authz -> authz
                // Permite acesso livre aos endpoints FHIR
                .requestMatchers("/hemogramas/**").permitAll()
                .requestMatchers("/fhir-management/**").permitAll()
                
                // Permite acesso aos endpoints de desenvolvimento
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                
                // Permite acesso aos endpoints de monitoramento
                .requestMatchers("/actuator/**").permitAll()
                
                // Permite acesso aos recursos estáticos
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                
                // Qualquer outra requisição requer autenticação
                .anyRequest().authenticated()
            )
            
            // Configura sessão como stateless (para APIs REST)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Desabilita headers de frame para permitir H2 Console
            .headers(headers -> headers
                .frameOptions().disable()
            );

        return http.build();
    }
}
