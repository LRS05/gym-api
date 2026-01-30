package com.project.gym.config;

import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.TokenType;
import com.project.gym.exception.CookieNotFoundException;
import com.project.gym.exception.InvalidTokenException;
import com.project.gym.exception.UserAlreadyAuthenticatedException;
import com.project.gym.repository.UserRepository;
import com.project.gym.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
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
        // Skip JWT filter for public endpoints (Swagger UI, API docs, and public integrations)
        if (isPublicPath(request.getServletPath()))
        {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * If the user does not send access token cookie (not authenticated) and the path is for authentication,
         * skip the JWT filter.
         */
        if (!jwtService.existsTokenCookie(TokenType.ACCESS, request.getCookies())
                && request.getServletPath().startsWith("/api/v1/auth"))
        {
            filterChain.doFilter(request, response);
            return;
        }

        try
        {
            /*
             * Retrieves the access token from cookies, ensuring that:
             * - The cookie exists.
             * - The token type is access.
             * - The token belongs to the corresponding user.
             * - The token is not expired.
             *
             * Throws an exception otherwise.
             *
             * If valid, sets a UsernamePasswordAuthenticationToken
             * in the SecurityContext.
             */
            authenticateRequest(request);
        }
        catch (CookieNotFoundException | UsernameNotFoundException | InvalidTokenException e)
        {
            sendErrorMessage(response, HttpStatus.UNAUTHORIZED, e.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void authenticateRequest(HttpServletRequest request)
    {
        String accessToken = jwtService.getTokenFromCookies(request.getCookies(), TokenType.ACCESS);

        UserEntity user =  userRepository.findByDni(jwtService.getSubject(accessToken))
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (!jwtService.isTokenValid(accessToken, user) || !jwtService.getTokenType(accessToken).equals("access"))
        {
            throw new InvalidTokenException("Invalid or expired access token.");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private boolean isPublicPath(String path)
    {
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api/v1/whatsapp");
    }

    private void sendErrorMessage(HttpServletResponse response, HttpStatus status, String message) throws IOException
    {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
