package com.example.application.Repository;

import com.example.application.Class.Jwt;
import com.example.application.Class.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface JwtRepository extends CrudRepository<Jwt, Integer> {
    @Query("from Jwt j where j.value = :value and j.deactivated = :Deactivated and j.expired = :Expired")
    Optional<Jwt> findByValueAndDeactivatedAndExpired(String value, boolean Deactivated, boolean Expired);

    @Query("from Jwt j where j.expired = :Expired and j.deactivated = :Deactivated and j.user.email = :email")
    Optional<Jwt> findByEmailAndDeactivatedAndExpired(String email, boolean Deactivated, boolean Expired);

    @Query("from Jwt j where j.user.email = :email")
    Stream<Jwt> findAllByEmail(String email);

    @Query("from Jwt j where j.refreshToken.value = :value")
    Optional<Jwt> findByRefreshToken(String value);

    void deleteAllByExpiredAndDeactivated(boolean expired, boolean deactivated);
}
