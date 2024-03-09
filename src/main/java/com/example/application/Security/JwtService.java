package com.example.application.Security;

import com.example.application.Class.Jwt;
import com.example.application.Class.User;
import com.example.application.Repository.JwtRepository;
import com.example.application.Service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class JwtService {

    public static final String BEARER = "Bearer";
    private UserService userService;
    private JwtRepository jwtRepository;

    public Map<String, String> generate(String username) {
        User user = this.userService.loadUserByUsername(username);
        final Map<String, String> jwtMap = this.generateJwt(user);
        final Jwt jwt = Jwt.builder()
                .Value(jwtMap.get(BEARER))
                .isDeactivated(false)
                .isExpired(false)
                .user(user)
                .build();
        this.jwtRepository.save(jwt);
        return jwtMap;
    }

    public String readUsername(String token) {
        return this.getClaim(token, Claims::getSubject);
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = this.getClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    private Map<String, String> generateJwt(User user) {
        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + 30 * 60 * 1000;

        final Map<String, Object> claims = Map.of(
                "nom", user.getName(),
                Claims.EXPIRATION, new Date(expirationTime),
                Claims.SUBJECT, user.getEmail()
        );

        final String bearer = Jwts.builder()
                .issuedAt(new Date(currentTime))
                .expiration(new Date(expirationTime))
                .subject(user.getEmail())
                .claims(claims)
                .signWith(this.getKeys()).compact();
        return Map.of(BEARER, bearer);
    }

    private SecretKey getKeys() {
        String ENCRYPTION_KEY = "26fd954bf058a0e245cb02f4439823d38a13249e390f29388bf1c5fc43761423";
        final byte[] decoder = Decoders.BASE64.decode(ENCRYPTION_KEY);
        return Keys.hmacShaKeyFor(decoder);
    }

    private <T> T getClaim(String token, Function<Claims, T> function) {
        final Claims claims = getAllClaims(token);
        return function.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.getKeys())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
