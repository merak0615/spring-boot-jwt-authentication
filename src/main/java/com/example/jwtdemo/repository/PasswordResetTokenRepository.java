package com.example.jwtdemo.repository;

import com.example.jwtdemo.models.PasswordResetToken;
import com.example.jwtdemo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    PasswordResetToken findByUser(User user);

    Long deleteByToken(String token);
}
