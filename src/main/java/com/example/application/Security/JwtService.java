package com.example.application.Security;

import com.example.application.Class.Jwt;
import com.example.application.Class.RefreshToken;
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
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Transactional
@Service
@AllArgsConstructor
public class JwtService {

    public static final String BEARER = "Bearer";
    public static final String REFRESH_TOKEN = "refreshToken";

    private UserService userService;
    private JwtRepository jwtRepository;

    // Method to get the token from the database
    public Jwt tokenByValue(String value) {
        return this.jwtRepository.findByValueAndDeactivatedAndExpired(
                        value,
                        false,
                        false)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    }

    // Method to generate the token and refresh token
    public Map<String, String> generate(String username) {

        User user = this.userService.loadUserByUsername(username);
        final Map<String, String> jwtMap = new java.util.HashMap<>(this.generateJwt(user));
        this.deactivateToken(user); // Deactivate the token in the database
        // Generate the refreshToken object
        RefreshToken refreshToken = RefreshToken.builder()
                .value(UUID.randomUUID().toString())
                .expired(false)
                .creationDate(Instant.now())
                .expirationDate(Instant.now().plusMillis(30 * 60 * 1000))
                .build();
        // Generate the Jwt object
        final Jwt jwt = Jwt.builder()
                .value(jwtMap.get(BEARER))
                .deactivated(false)
                .expired(false)
                .user(user)
                .refreshToken(refreshToken)
                .build();
        // Save the Jwt object in the database and put the refreshToken in the jwtMap
        this.jwtRepository.save(jwt);
        jwtMap.put(REFRESH_TOKEN, refreshToken.getValue());

        return jwtMap;
    }

    // Method to deactivate the token in the database
    private void deactivateToken(User user) { // TODO: add final List for RefreshToken to put true on expired

        final List<Jwt> jwtList = this.jwtRepository.findAllByEmail(user.getEmail()).peek(
                jwt -> {
                    jwt.setDeactivated(true);
                    jwt.setExpired(true);
                }
        ).collect(Collectors.toList());
        this.jwtRepository.saveAll(jwtList);
    }

    // Method to read the username from the token
    public String readUsername(String token) {
        return this.getClaim(token, Claims::getSubject);
    }

    // Method to check if the token is expired
    public boolean isTokenExpired(String token) {
        Date expirationDate = this.getClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    // Method to generate the jwt
    private Map<String, String> generateJwt(User user) {
        // Set the expiration time of the token to 30 minutes from the current time
        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + 30 * 60 * 1000;

        // Generate the claims
        final Map<String, Object> claims = Map.of(
                "nom", user.getName(),
                Claims.EXPIRATION, new Date(expirationTime),
                Claims.SUBJECT, user.getEmail()
        );

        // Generate the token
        final String bearer = Jwts.builder()
                .issuedAt(new Date(currentTime))
                .expiration(new Date(expirationTime))
                .subject(user.getEmail())
                .claims(claims)
                .signWith(this.getKeys()).compact();
        return Map.of(BEARER, bearer);
    }

    // Method to get the keys
    private SecretKey getKeys() {
        String ENCRYPTION_KEY = "26fd954bf058a0e245cb02f4439823d38a13249e390f29388bf1c5fc43761423"; // TODO : add to application.properties or make it random
        final byte[] decoder = Decoders.BASE64.decode(ENCRYPTION_KEY);
        return Keys.hmacShaKeyFor(decoder);
    }

    // Method to get the claim from the token
    private <T> T getClaim(String token, Function<Claims, T> function) {
        final Claims claims = getAllClaims(token);
        return function.apply(claims);
    }

    // Method to get all the claims from the token
    private Claims getAllClaims(String token) {
        // Parse the token and get the payload
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

    // Method to remove the expired tokens from the database
    @Scheduled(cron = "0 2 * * * *")
    public void removeToken(){ // TODO: add jwtRepository query for RefreshToken to remove expired token
        log.info("Removing expired tokens : ", Instant.now());
        this.jwtRepository.deleteAllByExpiredAndDeactivated(true, true);
    }

    // Method to refresh the token
    public Map<String, String> refreshToken(Map<String, String> refreshTokenRequest) {
        final Jwt jwt = this.jwtRepository.findByRefreshToken(refreshTokenRequest.get(REFRESH_TOKEN))
                .orElseThrow(() -> new RuntimeException("Token not found"));

                // Check if the token is expired
                if(jwt.getRefreshToken().isExpired() || jwt.getRefreshToken().getExpirationDate().isBefore(Instant.now())){
                    throw new RuntimeException("Token expired");
                }
                // Deactivate the token in the database
                this.deactivateToken(jwt.getUser());
                return this.generate(jwt.getUser().getEmail());
    }
}
