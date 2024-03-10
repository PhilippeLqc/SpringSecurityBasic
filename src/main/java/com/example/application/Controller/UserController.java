package com.example.application.Controller;

import com.example.application.Security.JwtService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.application.Class.User;
import com.example.application.DTO.AuthenficationDto;
import com.example.application.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private UserService userService;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;

    // Route pour créer un utilisateur
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signin")
    public void signin(@RequestBody User user) {
        log.info("User signed in"); // Log the event
        this.userService.signin(user); // Call the service to create the user
    }

    // Route pour se connecter
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody AuthenficationDto authenficationDto) {

        System.out.println("User logged in"); // Log the event
        // Create a token with the username and password
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(authenficationDto.username(), authenficationDto.password());
        Authentication authentication = this.authenticationManager.authenticate(token);
        // if the authentication is successful, generate a token
        if (authentication.isAuthenticated()) {
            return this.jwtService.generate(authenficationDto.username());
        }
        return Map.of("token", "bearer" + token); // Return the token
    }

    // Route pour se déconnecter
    @PostMapping("/deconnexion")
    public void deconnexion() {
        // Call the service to disconnect the user
        this.jwtService.deconnexion();
    }

    // Route pour rafraîchir le token
    @PostMapping("/refreshToken")
    public @ResponseBody Map<String, String> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        // Call the service to refresh the token
        return this.jwtService.refreshToken(refreshTokenRequest);
    }

}

