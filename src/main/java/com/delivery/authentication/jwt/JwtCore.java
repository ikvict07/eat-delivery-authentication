package com.delivery.authentication.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtCore {
    @Value("${auth.jwt.duration}")
    private int lifetime;

    @Value("${auth.jwt.secret}")
    private String secret;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
        key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setExpiration(new Date(new Date().getTime() + lifetime))
                .setSubject(email)
                .setIssuedAt(new Date())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenForApi() {
        return Jwts.builder()
                .setExpiration(new Date(new Date().getTime() + lifetime))
                .setSubject("Allowed")
                .setIssuedAt(new Date())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
