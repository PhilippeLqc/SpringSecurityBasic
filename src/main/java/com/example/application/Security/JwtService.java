package com.example.application.Security;

import com.example.application.Class.Jwt;
import com.example.application.Class.User;
import com.example.application.Repository.JwtRepository;
import com.example.application.Service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Transactional
@Service
@AllArgsConstructor
public class JwtService {

    public static final String BEARER = "Bearer";
    private UserService userService;
    private JwtRepository jwtRepository;

    public Jwt tokenByValue(String value) {
        return this.jwtRepository.findByValueAndDeactivatedAndExpired(
                        value,
                        false,
                        false)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    }

    public Map<String, String> generate(String username) {
        User user = this.userService.loadUserByUsername(username);
        final Map<String, String> jwtMap = this.generateJwt(user);
        this.deactivateToken(user);
        final Jwt jwt = Jwt.builder()
                .value(jwtMap.get(BEARER))
                .deactivated(false)
                .expired(false)
                .user(user)
                .build();
        this.jwtRepository.save(jwt);
        return jwtMap;
    }

    private void deactivateToken(User user) {
        final List<Jwt> jwtList = this.jwtRepository.findAllByEmail(user.getEmail()).peek(
                jwt -> {
                    jwt.setDeactivated(true);
                    jwt.setExpired(true);
                }
        ).collect(Collectors.toList());
        this.jwtRepository.saveAll(jwtList);
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

    // Logout the user by deactivating the token in the database
    public void deconnexion() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Jwt jwt = this.jwtRepository.findByEmailAndDeactivatedAndExpired(
                user.getEmail(),
                false,
                false
        ).orElseThrow(() -> new RuntimeException("Token not found"));
        jwt.setDeactivated(true);
        jwt.setExpired(true);
        this.jwtRepository.save(jwt);
    }
    @Scheduled(cron = "@daily")
    public void removeToken(){
        log.info("Removing expired tokens : ", Instant.now());
        this.jwtRepository.deleteAllByExpiredAndDeactivated(true, true);
    }
}
