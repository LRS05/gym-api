package com.project.gym.config;

import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.TokenType;
import com.project.gym.repository.UserRepository;
import com.project.gym.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter
{
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException
    {
        if (isAuthenticated())
        {
            filterChain.doFilter(request, response);
            return;
        }

        if (isPublicPath(request))
        {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getCookies() == null)
        {
            sendErrorMessage(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT cookie not found.");
            return;
        }

        try
        {
            authenticateRequest(request);
        }
        catch (UsernameNotFoundException e)
        {
            sendErrorMessage(response, HttpServletResponse.SC_NOT_FOUND, "User not found.");
            return;
        }
        catch (JwtException e)
        {
            sendErrorMessage(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticated()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    private boolean isPublicPath(HttpServletRequest request)
    {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api/v1/whatsapp");
    }

    private void authenticateRequest(HttpServletRequest request)
    {
        String accessToken = jwtService.getTokenFromCookies(request.getCookies(), TokenType.ACCESS);

        String subject = jwtService.getSubject(accessToken);
        UserEntity user = userRepository.findByDni(subject)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (jwtService.isTokenValid(accessToken, user) && jwtService.getTokenType(accessToken).equals("access"))
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }

    private void sendErrorMessage(HttpServletResponse response, int status, String message) throws IOException
    {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
