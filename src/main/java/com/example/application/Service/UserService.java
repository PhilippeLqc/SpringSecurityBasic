package com.example.application.Service;

import com.example.application.Class.Role;
import com.example.application.Class.User;
import com.example.application.Repository.UserRepository;
import com.example.application.RoleType;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    public void signin(User user) {
        //Verify if Email is valid
        if(!user.getEmail().matches("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")) {
            throw new RuntimeException("Email is not valid");
        }
        Optional<User> OptionalUser= this.userRepository.findByEmail(user.getEmail());
        if(OptionalUser.isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String BcryptPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(BcryptPassword);

        Role roleUser = new Role();
        roleUser.setLabel(RoleType.User);
        user.setRole(roleUser);

        this.userRepository.save(user);

    }

    // Method to load the user by email
    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));
    }
}
