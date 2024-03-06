package com.example.application.Controller;

import com.example.application.Class.User;
import com.example.application.DTO.AuthenficationDto;
import com.example.application.Service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private UserService userService;
    private AuthenticationManager authenticationManager;

    @PostMapping("/signin")
    public void signin(@RequestBody User user) {
        log.info("User signed in");
        this.userService.signin(user);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody AuthenficationDto authenficationDto) {
        System.out.println("User logged in");
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(authenficationDto.username(), authenficationDto.password());
        Authentication authentication = this.authenticationManager.authenticate(token);
        System.out.println("Authentication: " + authentication);
        return Map.of("token", "Bearer " + authentication.getCredentials());
    }
}

