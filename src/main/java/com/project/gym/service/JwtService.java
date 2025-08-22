package com.project.gym.service;

import com.project.gym.entity.UserEntity;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
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

    public String generateAccessToken(UserEntity user)
    {
        return buildToken(user, accessExpiration, "access");
    }

    public String generateRefreshToken(UserEntity user)
    {
        return buildToken(user, refreshExpiration, "refresh");
    }

    public boolean isRefreshToken(String token)
    {
        try
        {
            String type = Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("type", String.class);
            return "refresh".equals(type);
        }
        catch (JwtException | IllegalArgumentException e)
        {
            return false;
        }
    }

    public boolean isTokenValid(String token, UserEntity user)
    {
        try
        {
            String tokenDni = extractSubject(token);
            return user.getDni().equals(tokenDni) && extractExpiration(token).after(new Date());
        }
        catch (JwtException e)
        {
            return false;
        }
    }

    public String extractSubject(String token)
    {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
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

    private String buildToken(UserEntity user, long expiration, String tokenType)
    {
        return Jwts
                .builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getDni())
                .claim("role", user.getRole())
                .claim("type", tokenType)
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
