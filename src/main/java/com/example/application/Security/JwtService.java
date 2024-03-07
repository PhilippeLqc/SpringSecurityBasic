package com.example.application.Security;

import com.example.application.Class.User;
import com.example.application.Service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
@AllArgsConstructor
public class JwtService {

    private UserService userService;
    private final String ENCRYPTION_KEY = "26fd954bf058a0e245cb02f4439823d38a13249e390f29388bf1c5fc43761423";

    public Map<String, String> generate(String username) {
        User user = this.userService.loadUserByUsername(username);
        return this.generateJwt(user);
    }

    private Map<String, String> generateJwt(User user) {
        final Map<String, String> claims = Map.of(
                "nom", user.getName(),
                "email", user.getEmail()
        );
        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + 30 * 60 * 1000;

        final String bearer = Jwts.builder()
                .issuedAt(new Date(currentTime))
                .expiration(new Date(expirationTime))
                .subject(user.getEmail())
                .claims(claims)
                .signWith(this.getKeys()).compact();
        return Map.of("token", "Bearer " + bearer);
    }

    private Key getKeys() {
        final byte[] decoder = Decoders.BASE64.decode(ENCRYPTION_KEY);
        return Keys.hmacShaKeyFor(decoder);
    }
}
