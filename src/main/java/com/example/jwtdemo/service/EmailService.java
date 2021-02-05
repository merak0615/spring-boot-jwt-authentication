package com.example.jwtdemo.service;

import com.example.jwtdemo.models.PasswordResetToken;
import com.example.jwtdemo.models.User;
import com.example.jwtdemo.models.VerificationToken;
import com.example.jwtdemo.repository.PasswordResetTokenRepository;
import com.example.jwtdemo.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    VerificationTokenRepository verificationTokenRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    SpringTemplateEngine templateEngine;


    public void  sendVerificationMail(User user) throws MessagingException {
        VerificationToken verificationToken = verificationTokenRepository.findByUser(user);

        //check if the user have a token
        if (verificationToken != null) {
            String token = verificationToken.getToken();
            Context context = new Context();
            context.setVariable("title", "Verify your email address");
            context.setVariable("link", "http://localhost:3000/activation/" + token);

            //create a HTML template and pass the variables to it
            String body = templateEngine.process("verification", context);

            //send the verification email
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("email address verification");
            helper.setText(body, true);
            javaMailSender.send(message);
        }
    }

    public void  sendPasswordResetMail(User user) throws MessagingException {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByUser(user);

        //check if the user have a token
        if (passwordResetToken != null) {
            String token = passwordResetToken.getToken();
            Context context = new Context();
            context.setVariable("title", "Reset your account password");
            context.setVariable("link", "http://localhost:3000/update/" + token);

            //create a HTML template and pass the variables to it
            String body = templateEngine.process("passwordreset", context);

            //send the verification email
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("password reset");
            helper.setText(body, true);
            javaMailSender.send(message);
        }
    }
}
