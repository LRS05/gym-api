package com.project.gym.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MdcFilter extends HttpFilter
{
    @Override
    protected void doFilter(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        try
        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String ip = request.getHeader("X-Forwarded-For");

            if (authentication != null && authentication.isAuthenticated())
            {
                String role = authentication.getAuthorities().stream()
                        .map(Object::toString)
                        .filter(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_STAFF") || a.equals("ROLE_USER"))
                        .findFirst()
                        .orElse("ROLE_UNKNOWN");

                MDC.put("dni", authentication.getName());
                MDC.put("role", role);
            }

            MDC.put("method", request.getMethod());
            MDC.put("uri", request.getRequestURI());
            MDC.put("ip", ip);

            chain.doFilter(request, response);
        }
        finally
        {
            MDC.clear();
        }
    }
}
