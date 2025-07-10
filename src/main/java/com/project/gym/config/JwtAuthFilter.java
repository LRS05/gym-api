package com.project.gym.config;

import ch.qos.logback.core.util.StringUtil;
import com.project.gym.entity.UserEntity;
import com.project.gym.repository.UserRepository;
import com.project.gym.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter
{
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException
    {

        String path = request.getServletPath();

        if (path.startsWith("/api/v1/auth") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs"))
        {
            filterChain.doFilter(request, response);
            return;
        }


        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Si el Header es invalido, el filtro se evita.
        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header.");
            return;
        }

        String jwtToken = authHeader.substring(7);
        String subject = jwtService.extractSubject(jwtToken);

        if (subject == null)
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid subject.");
            return;
        }

        UserEntity user = userRepository.findByDni(subject).orElse(null);

        if (user == null)
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User not found.");
            return;
        }

        if (jwtService.isTokenValid(jwtToken, user))
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );

            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}
