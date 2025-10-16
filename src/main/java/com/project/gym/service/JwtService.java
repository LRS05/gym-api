package com.project.gym.service;

import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.TokenType;
import com.project.gym.exception.CookieNotFoundException;
import com.project.gym.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class JwtService
{
    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    public ResponseCookie generateAccessTokenCookie(UserEntity user)
    {
        String accessToken = buildToken(user, accessExpiration, TokenType.ACCESS);
        return buildCookie("access-token", accessToken, 15);
    }

    public ResponseCookie generateRefreshTokenCookie(UserEntity user)
    {
        String refreshToken = buildToken(user, refreshExpiration, TokenType.REFRESH);
        return buildCookie("refresh-token", refreshToken, 1440);
    }

    public ResponseCookie emptyCookie(String name)
    {
        return buildCookie(name, "", 0);
    }

    public String getTokenFromCookies(Cookie[] cookies, TokenType tokenType)
    {
        if (cookies == null)
        {
            throw new CookieNotFoundException("Cookies not found.");
        }

        // Throws exception if the token is not found.
        return findCookieToken(cookies, tokenType);
    }

    public boolean isTokenValid(String token, UserEntity user)
    {
        try
        {
            String tokenDni = getSubject(token);
            return user.getDni().equals(tokenDni) && getExpiration(token).after(new Date());
        }
        catch (JwtException | InvalidTokenException e)
        {
            return false;
        }
    }

    public String getSubject(String token)
    {
        return parseToken(token).getSubject();
    }

    public String getTokenType(String token)
    {
        return parseToken(token).get("type", String.class).toLowerCase();
    }

    private Date getExpiration(String token)
    {
        return parseToken(token).getExpiration();
    }

    private Claims parseToken(String token)
    {
        try
        {
            return Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
        catch (JwtException e)
        {
            throw new InvalidTokenException("Invalid or expired JWT token.");
        }
    }

    private String buildToken(UserEntity user, long expiration, TokenType type)
    {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getDni())
                .claim("role", user.getRole())
                .claim("type", type.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey())
                .compact();
    }

    private ResponseCookie buildCookie(String name, String token, int durationOfMinutes)
    {
        return ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMinutes(durationOfMinutes))
                .build();
    }

    private String findCookieToken(Cookie[] cookies, TokenType tokenType)
    {
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals(tokenType.name().toLowerCase() + "-token"))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new CookieNotFoundException(tokenType.name() + " token cookie not found."));
    }

    private SecretKey secretKey()
    {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
