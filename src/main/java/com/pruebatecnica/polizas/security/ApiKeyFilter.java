package com.pruebatecnica.polizas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Seguridad minima exigida por el enunciado: exige el header x-api-key en cada peticion. */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "x-api-key";

    @Value("${security.api-key}")
    private String apiKeyEsperada;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER_NAME);
        if (apiKey == null || !apiKey.equals(apiKeyEsperada)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Header 'x-api-key' ausente o invalido\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
