package org.uvhnael.ktal.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(final jakarta.servlet.http.HttpServletRequest request, final jakarta.servlet.http.HttpServletResponse response, final jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {
        String requestUri = request.getRequestURI();

        // Check if the request URI contains "uploads"
        if (!requestUri.contains("uploads")) {
            String message = String.format("\u001B[33mIncoming request: %s %s\u001B[0m", request.getMethod(), request.getRequestURI());
            logger.info(message);
        }


        filterChain.doFilter(request, response);
    }
}