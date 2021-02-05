package com.example.jwtdemo.controllers;

import com.example.jwtdemo.models.PasswordResetToken;
import com.example.jwtdemo.models.User;
import com.example.jwtdemo.models.VerificationToken;
import com.example.jwtdemo.payload.request.ResetRequest;
import com.example.jwtdemo.payload.response.MessageResponse;
import com.example.jwtdemo.service.EmailService;
import com.example.jwtdemo.service.PasswordResetTokenService;
import com.example.jwtdemo.service.UserService;
import com.example.jwtdemo.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/mail")
public class EmailController {
    @Autowired
    UserService userService;

    @Autowired
    PasswordResetTokenService passwordResetTokenService;

    @Autowired
    VerificationTokenService verificationTokenService;

    @Autowired
    EmailService emailService;

    @GetMapping("/activation")
    public ResponseEntity<?> activation(@RequestParam("token") String token) {
        //create html page activation
        VerificationToken verificationToken = verificationTokenService.findByToken(token);
        if (verificationToken == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Your verification token is invalid"));
        } else {
            User user = verificationToken.getUser();
            if (!user.isEnabled()) {
                Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
                if (verificationToken.getExpiryDate().before(currentTimestamp)) {
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Your verification token is expired"));
                } else {
                    user.setEnabled(true);
                    userService.save(user);
                    return ResponseEntity.ok(new MessageResponse("Your account is successfully activated"));
                }
            } else {
                //the user account has already activated
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Your account is already activated"));
            }
        }
    }

    @GetMapping("/sendtoken")
    public ResponseEntity<?> passwordReset(@RequestParam("email") String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not Found with email: " + email));

        try {
            //create or update passwordReset token
            PasswordResetToken passwordResetToken = passwordResetTokenService.findByUser(user);
            String token = UUID.randomUUID().toString();
            if (passwordResetToken != null) {
                passwordResetTokenService.updatePasswordResetToken(token, passwordResetToken);
            } else {
                passwordResetTokenService.save(token, user);
            }
            //send passwordReset email
            emailService.sendPasswordResetMail(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(new MessageResponse("Your message has been sent"));
    }

    @PostMapping("/passwordreset")
    public ResponseEntity<?> showChangePasswordPage(@Valid @RequestBody ResetRequest resetRequest) {
        PasswordResetToken passwordResetToken = passwordResetTokenService.findByToken(resetRequest.getToken());

        if (passwordResetToken == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Your passwordReset token is invalid"));
        } else {
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            if (passwordResetToken.getExpiryDate().before(currentTimestamp)) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Your passwordReset token is expired"));
            } else {
                //user password has already changed
                User user = passwordResetTokenService.getUserByPasswordResetToken(resetRequest.getToken());
                userService.changeUserPassword(user, resetRequest.getPassword());

                //delete passwordReset Token
                passwordResetTokenService.deleteByToken(resetRequest.getToken());
                return ResponseEntity.ok(new MessageResponse("Your password has already been changed"));
            }
        }
    }
}
