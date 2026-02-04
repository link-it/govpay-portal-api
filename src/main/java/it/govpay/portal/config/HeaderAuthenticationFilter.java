package it.govpay.portal.config;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {
	
	public static final String TINIT_PREFIX = "TINIT-";

    private final SecurityProperties.SpidHeaders spidHeaders;

    public HeaderAuthenticationFilter(SecurityProperties.SpidHeaders spidHeaders) {
        this.spidHeaders = spidHeaders;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

    	log.debug("{} {} - Lettura del principal SPID dall'Header [{}]...",
    			request.getMethod(), request.getRequestURI(), spidHeaders.getFiscalNumber());
        String fiscalNumber = request.getHeader(spidHeaders.getFiscalNumber());

        if (fiscalNumber != null && !fiscalNumber.isBlank()) {
        	
   			int indexOfTINIT = fiscalNumber.indexOf(TINIT_PREFIX);
   			if(indexOfTINIT > -1) {
   				fiscalNumber = fiscalNumber.substring(indexOfTINIT + TINIT_PREFIX.length());
   			}
        	
            SpidUserDetails spidUserDetails = new SpidUserDetails(
                    fiscalNumber,
                    request.getHeader(spidHeaders.getName()),
                    request.getHeader(spidHeaders.getFamilyName()),
                    request.getHeader(spidHeaders.getEmail()),
                    request.getHeader(spidHeaders.getMobilePhone()),
                    request.getHeader(spidHeaders.getAddress())
            );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    spidUserDetails,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        log.debug("Letto Principal: [{}]", fiscalNumber);
        filterChain.doFilter(request, response);
    }

}
