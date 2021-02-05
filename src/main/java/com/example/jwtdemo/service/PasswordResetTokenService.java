package com.example.jwtdemo.service;

import com.example.jwtdemo.models.PasswordResetToken;
import com.example.jwtdemo.models.User;
import com.example.jwtdemo.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Calendar;

@Service
public class PasswordResetTokenService {

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public PasswordResetToken findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Transactional
    public PasswordResetToken findByUser(User user) {
        return passwordResetTokenRepository.findByUser(user);
    }

    @Transactional
    public void save(String token, User user) {
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);

        //set expiry date to 24 hours
        passwordResetToken.setExpiryDate(calculateExpiryDate(24*60));
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Transactional
    public void updatePasswordResetToken(String token, PasswordResetToken passwordResetToken) {
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(calculateExpiryDate(24*60));
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Transactional
    public User getUserByPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token).getUser();
    }

    @Transactional
    public Long deleteByToken(String token) {
         return passwordResetTokenRepository.deleteByToken(token);
    }

    private Timestamp calculateExpiryDate(int expiryInMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expiryInMinutes);
        return new Timestamp(calendar.getTime().getTime());
    }
}
