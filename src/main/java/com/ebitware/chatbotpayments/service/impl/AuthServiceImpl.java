package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.model.AuthDTO;
import com.ebitware.chatbotpayments.model.UserDTO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long tokenExpirationTime;

    public AuthDTO generateUserToken(UserDTO data) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("firstName", data.getFirstName());
        claims.put("lastName", data.getLastName());
        claims.put("email", data.getEmail());
        claims.put("companyId", data.getCompanyId());
        claims.put("roleId", data.getRoleId());
        claims.put("isSuper", data.getIsSuper());

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(data.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpirationTime * 1000))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();

        AuthDTO authDto = new AuthDTO();
        authDto.setExpires(System.currentTimeMillis() + tokenExpirationTime * 1000);
        authDto.setKey(token);

        return authDto;
    }
}
