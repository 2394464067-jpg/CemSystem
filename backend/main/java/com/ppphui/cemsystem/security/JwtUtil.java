package com.ppphui.cemsystem.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private Long expire;

    public String generateToken(Integer userId,String userName,String role,String factoryName){
        Date now=new Date();
        Date expireDate=new Date(now.getTime()+expire);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username",userName)
                .claim("factoryName",factoryName)
                .claim("role",role)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

}
