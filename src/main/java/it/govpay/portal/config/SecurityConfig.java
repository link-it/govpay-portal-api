package it.govpay.portal.config;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.XXssConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.portal.gde.Costanti;
import it.govpay.portal.gde.service.GdeService;
import it.govpay.portal.repository.VersamentoRepository;
import it.govpay.portal.security.hardening.matcher.AvvisiRequestMatcher;
import it.govpay.portal.security.hardening.matcher.HardeningRequestMatcher;
import it.govpay.portal.service.ConfigurazioneService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final ConfigurazioneService configurazioneService;
    private final VersamentoRepository versamentoRepository;
    private final GdeService gdeService;

    public SecurityConfig(SecurityProperties securityProperties,
            ConfigurazioneService configurazioneService,
            VersamentoRepository versamentoRepository,
            GdeService gdeService) {
        this.securityProperties = securityProperties;
        this.configurazioneService = configurazioneService;
        this.versamentoRepository = versamentoRepository;
        this.gdeService = gdeService;
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

    /**
     * LogoutFilter custom che intercetta GET /logout e GET /logout/{urlID}.
     * Replica il comportamento della configurazione XML di api-user:
     * - SecurityContextLogoutHandler: pulisce SecurityContext e invalida la sessione
     * - CookieClearingLogoutHandler: rimuove il cookie JSESSIONID
     * - PortalLogoutSuccessHandler: gestisce la risposta (200 o 303 redirect)
     */
    @Bean
    public LogoutFilter portalLogoutFilter() {
        SecurityContextLogoutHandler securityContextHandler = new SecurityContextLogoutHandler();
        securityContextHandler.setInvalidateHttpSession(true);
        securityContextHandler.setClearAuthentication(true);

        CookieClearingLogoutHandler cookieHandler = new CookieClearingLogoutHandler("JSESSIONID");

        PortalLogoutSuccessHandler successHandler = new PortalLogoutSuccessHandler(securityProperties, gdeService);

        LogoutFilter logoutFilter = new LogoutFilter(successHandler, securityContextHandler, cookieHandler);
        logoutFilter.setFilterProcessesUrl("/logout");
        logoutFilter.setLogoutRequestMatcher(new OrRequestMatcher(
                new AntPathRequestMatcher("/logout", "GET"),
                new AntPathRequestMatcher("/logout/**", "GET")
        ));

        return logoutFilter;
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
                .invalidSessionStrategy(new NotAuthorizedInvalidSessionStrategy())
                .maximumSessions(2)
                .expiredSessionStrategy(new NotAuthorizedSessionInformationExpiredStrategy()))
            .addFilterBefore(headerAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // ==========================================
                // Endpoint SOLO AUTENTICATI (SPID)
                // ==========================================
                // Login
                .requestMatchers(HttpMethod.GET, "/login").authenticated()
                .requestMatchers(HttpMethod.GET, "/login/{urlID}").authenticated()
                // Profilo utente
                .requestMatchers(HttpMethod.GET, "/profilo").authenticated()
                // Logout: gestito dal LogoutFilter (non raggiunge l'authorization filter)
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
                .xssProtection(XXssConfig::disable)
            )
            .logout(logout -> logout.disable())
            .addFilterAfter(portalLogoutFilter(), LogoutFilter.class)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    tracciaFallimentoAutenticazione(request, authException);
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), Map.of(
                            "categoria", "AUTORIZZAZIONE",
                            "codice", "403",
                            "descrizione", "Accesso negato",
                            "dettaglio", "Autenticazione richiesta"
                    ));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    tracciaFallimentoAutenticazione(request, accessDeniedException);
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), Map.of(
                            "categoria", "AUTORIZZAZIONE",
                            "codice", "403",
                            "descrizione", "Accesso negato",
                            "dettaglio", accessDeniedException.getMessage()
                    ));
                })
            )
            .anonymous(anonymous -> anonymous.principal("UTENTE_ANONIMO"))
            // Filtro per forzare il caricamento del CSRF token su ogni risposta.
            // Spring Security 6 usa token deferred: senza questo filtro il cookie
            // XSRF-TOKEN non viene inviato finche' il token non e' esplicitamente acceduto.
            .addFilterAfter(new CsrfCookieFilter(), org.springframework.security.web.csrf.CsrfFilter.class);

        return http.build();
    }

    private void tracciaFallimentoAutenticazione(HttpServletRequest request, Exception exception) {
        String tipoEvento = resolveOperationType(request.getRequestURI());
        if (tipoEvento != null) {
            OffsetDateTime now = OffsetDateTime.now();
            gdeService.saveEventKo(tipoEvento, now, now,
                    request, HttpStatus.FORBIDDEN.value(), exception, null, null);
        }
    }

    private static final Map<String, String> AUTH_ENDPOINT_MAP = Map.of(
            "/profilo", Costanti.OP_GET_PROFILO,
            "/logout", Costanti.OP_LOGOUT
    );

    private String resolveOperationType(String requestUri) {
        // Controlla match esatto o con prefisso (es. /login/urlID)
        for (Map.Entry<String, String> entry : AUTH_ENDPOINT_MAP.entrySet()) {
            if (requestUri.equals(entry.getKey()) || requestUri.startsWith(entry.getKey() + "/")) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Filtro che forza la materializzazione del CSRF token, garantendo che il cookie
     * XSRF-TOKEN venga sempre inviato al client (necessario per SPA).
     */
    static class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                // Forza la generazione del token (e quindi del cookie)
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }

}
