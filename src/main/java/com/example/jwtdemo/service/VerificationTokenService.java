package com.example.jwtdemo.service;

import com.example.jwtdemo.models.User;
import com.example.jwtdemo.models.VerificationToken;
import com.example.jwtdemo.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Calendar;

@Service
public class VerificationTokenService {

    @Autowired
    VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public VerificationToken findByToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    @Transactional
    public void save(String token, User user) {
        VerificationToken verificationToken = new VerificationToken(token, user);

        //set expiry date to 24 hours
        verificationToken.setExpiryDate(calculateExpiryDate(24*60));
        verificationTokenRepository.save(verificationToken);
    }

    private Timestamp calculateExpiryDate(int expiryInMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expiryInMinutes);
        return new Timestamp(calendar.getTime().getTime());
    }
}
