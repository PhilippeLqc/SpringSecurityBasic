package com.example.application.Repository;

import org.springframework.data.repository.CrudRepository;
import com.example.application.Class.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long>{

    // find by email
    Optional<User> findByEmail(String email);
}
