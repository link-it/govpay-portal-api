package it.govpay.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final SecurityProperties securityProperties;

    public SecurityConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowUrlEncodedPercent(true);
        return firewall;
    }

    @Bean
    public HeaderAuthenticationFilter headerAuthenticationFilter() {
        return new HeaderAuthenticationFilter(securityProperties.getSpidHeaders());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().changeSessionId()
                .maximumSessions(2))
            .addFilterBefore(headerAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // ==========================================
                // Endpoint SOLO AUTENTICATI (SPID)
                // ==========================================
                // Profilo utente
                .requestMatchers(HttpMethod.GET, "/profilo").authenticated()
                // Logout
                .requestMatchers(HttpMethod.GET, "/logout").authenticated()
                // Elenco pendenze utente
                .requestMatchers(HttpMethod.GET, "/pendenze/{idDominio}").authenticated()
                // Dettaglio pendenza
                .requestMatchers(HttpMethod.GET, "/pendenze/{idDominio}/{numeroAvviso}").authenticated()

                // ==========================================
                // Endpoint PUBLIC + AUTENTICATI
                // ==========================================
                // Domini (anagrafica)
                .requestMatchers(HttpMethod.GET, "/domini").permitAll()
                .requestMatchers(HttpMethod.GET, "/domini/{idDominio}").permitAll()
                .requestMatchers(HttpMethod.GET, "/domini/{idDominio}/logo").permitAll()
                .requestMatchers(HttpMethod.GET, "/domini/{idDominio}/tipiPendenza").permitAll()
                .requestMatchers(HttpMethod.GET, "/domini/{idDominio}/tipiPendenza/{idTipoPendenza}").permitAll()
                // Creazione pendenza spontanea (con reCAPTCHA lato applicativo)
                .requestMatchers(HttpMethod.POST, "/pendenze/{idDominio}/{idTipoPendenza}").permitAll()
                // Avviso di pagamento
                .requestMatchers(HttpMethod.GET, "/pendenze/{idDominio}/{numeroAvviso}/avviso").permitAll()
                // Ricevuta PDF
                .requestMatchers(HttpMethod.GET, "/pendenze/{idDominio}/{numeroAvviso}/ricevuta").permitAll()

                // ==========================================
                // Risorse statiche e documentazione
                // ==========================================
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll()
                .requestMatchers("/*.yaml", "/*.json").permitAll()
                .requestMatchers("/index.html", "/*.png", "/*.css", "/*.js").permitAll()
                .requestMatchers("/*.css.map", "/*.js.map").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // Tutto il resto negato
                .anyRequest().denyAll()
            )
            .headers(headers -> headers
                .contentTypeOptions(cto -> cto.disable())
                .frameOptions(fo -> fo.sameOrigin())
                .xssProtection(xss -> xss.disable())
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
            )
            .anonymous(anonymous -> anonymous.principal("UTENTE_ANONIMO"));

        return http.build();
    }

}
