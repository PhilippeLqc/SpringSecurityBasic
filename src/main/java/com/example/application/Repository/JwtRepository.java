package com.example.application.Repository;

import com.example.application.Class.Jwt;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JwtRepository extends CrudRepository<Jwt, Long>{
    Optional<Jwt> findByValue(String value);
}
