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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import it.govpay.portal.repository.VersamentoRepository;
import it.govpay.portal.security.hardening.matcher.AvvisiRequestMatcher;
import it.govpay.portal.security.hardening.matcher.HardeningRequestMatcher;
import it.govpay.portal.service.ConfigurazioneService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final ConfigurazioneService configurazioneService;
    private final VersamentoRepository versamentoRepository;

    public SecurityConfig(SecurityProperties securityProperties,
            ConfigurazioneService configurazioneService,
            VersamentoRepository versamentoRepository) {
        this.securityProperties = securityProperties;
        this.configurazioneService = configurazioneService;
        this.versamentoRepository = versamentoRepository;
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

    /**
     * RequestMatcher per la creazione pendenza spontanea con controllo ReCaptcha.
     * Se l'utente è autenticato, il controllo viene bypassato.
     */
    @Bean
    public HardeningRequestMatcher pendenzaSpontaneaRequestMatcher() {
        return new HardeningRequestMatcher("/pendenze/*/*", HttpMethod.POST, configurazioneService);
    }

    /**
     * RequestMatcher per l'avviso di pagamento con controlli hardening.
     * Verifica UUID, ReCaptcha e sessione per i PDF.
     * Se l'utente è autenticato, i controlli vengono bypassati.
     */
    @Bean
    public AvvisiRequestMatcher avvisoRequestMatcher() {
        return new AvvisiRequestMatcher("/pendenze/*/*/avviso", HttpMethod.GET,
                configurazioneService, versamentoRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF token repository che salva il token in un cookie leggibile da JavaScript.
        // HttpOnly=false e' necessario per permettere a SPA/JavaScript di leggere il token
        // dal cookie XSRF-TOKEN e inviarlo nell'header X-XSRF-TOKEN nelle richieste POST/PUT/DELETE.
        // Questo e' il pattern standard per protezione CSRF in applicazioni REST con frontend SPA.
        // Il token CSRF non e' un segreto di sessione: anche se letto via XSS, non permette
        // di eseguire CSRF perche' l'attaccante non puo' generare richieste cross-origin valide.
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse(); // NOSONAR - HttpOnly=false intenzionale per SPA
        // Handler per supportare protezione BREACH
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName("_csrf");

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository)
                .csrfTokenRequestHandler(csrfHandler)
                // NOTA: Spring Security 6 ignora automaticamente i safe methods (GET/HEAD/OPTIONS/TRACE)
                // quindi non e' necessario escluderli esplicitamente
            )
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
                // Endpoint PUBLIC + AUTENTICATI con HARDENING
                // ==========================================
                // Creazione pendenza spontanea (ReCaptcha per anonimi)
                .requestMatchers(pendenzaSpontaneaRequestMatcher()).permitAll()
                // Avviso di pagamento (UUID + ReCaptcha + session per anonimi)
                .requestMatchers(avvisoRequestMatcher()).permitAll()

                // ==========================================
                // Endpoint PUBLIC + AUTENTICATI (senza hardening)
                // ==========================================
                // Domini (anagrafica)
                .requestMatchers(HttpMethod.GET, "/domini").permitAll()
                .requestMatchers(HttpMethod.GET, "/domini/{idDominio}").permitAll()
                .requestMatchers(HttpMethod.GET, "/domini/{idDominio}/logo").permitAll()
                .requestMatchers(HttpMethod.GET, "/domini/{idDominio}/tipiPendenza").permitAll()
                .requestMatchers(HttpMethod.GET, "/domini/{idDominio}/tipiPendenza/{idTipoPendenza}").permitAll()
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
