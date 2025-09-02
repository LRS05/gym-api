package com.project.gym.service;

import com.project.gym.entity.UserEntity;
import com.project.gym.entity.enums.TokenType;
import com.project.gym.exception.CookieNotFoundException;
import com.project.gym.exception.InvalidTokenException;
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
import java.util.Date;
import java.util.Map;
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

    public String generateAccessToken(UserEntity user)
    {
        return buildToken(user, accessExpiration, TokenType.ACCESS_TOKEN);
    }

    public String generateRefreshToken(UserEntity user)
    {
        return buildToken(user, refreshExpiration, TokenType.REFRESH_TOKEN);
    }

    public Map<String, ResponseCookie> generateTokenCookies(String accessToken, String refreshToken)
    {
        ResponseCookie accessCookie = generateAccessTokenCookie(accessToken);
        ResponseCookie refreshCookie = generateRefreshTokenCookie(refreshToken);

        return Map.of("access_token", accessCookie,
                "refresh_token", refreshCookie);
    }

    public ResponseCookie generateAccessTokenCookie(String token)
    {
        return ResponseCookie.from(TokenType.ACCESS_TOKEN.name(), token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .build();
    }

    public ResponseCookie generateRefreshTokenCookie(String token)
    {
        return ResponseCookie.from(TokenType.REFRESH_TOKEN.name(), token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();
    }

    public ResponseCookie emptyCookie(String name)
    {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
    }

    public String extractCookiesToken(Cookie[] cookies, TokenType tokenType)
    {
        if (cookies == null)
        {
            throw new CookieNotFoundException("No cookies found.");
        }

        for (Cookie cookie : cookies)
        {
            if (tokenType.name().equals(cookie.getName()))
            {
                return cookie.getValue();
            }
        }
        throw new CookieNotFoundException(tokenType.name() + " cookie not found.");
    }

    public boolean isTokenValid(String token, UserEntity user)
    {
        try
        {
            String tokenDni = extractSubject(token);
            return user.getDni().equals(tokenDni) && extractExpiration(token).after(new Date());
        }
        catch (JwtException | InvalidTokenException e)
        {
            return false;
        }
    }

    public String extractSubject(String token)
    {
        try
        {
            return Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        }
        catch (JwtException e)
        {
            throw new InvalidTokenException("Invalid JWT Token.");
        }
    }

    private Date extractExpiration(String token)
    {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    private String buildToken(UserEntity user, long expiration, TokenType type)
    {
        return Jwts
                .builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getDni())
                .claim("role", user.getRole())
                .claim("type", type.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey())
                .compact();
    }

    private SecretKey secretKey()
    {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
