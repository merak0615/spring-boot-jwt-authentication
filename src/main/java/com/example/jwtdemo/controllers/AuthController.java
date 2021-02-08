package com.example.jwtdemo.controllers;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.example.jwtdemo.models.ERole;
import com.example.jwtdemo.models.PasswordResetToken;
import com.example.jwtdemo.models.Role;
import com.example.jwtdemo.models.User;
import com.example.jwtdemo.payload.request.LoginRequest;
import com.example.jwtdemo.payload.request.PasswordRequest;
import com.example.jwtdemo.payload.request.SignupRequest;
import com.example.jwtdemo.payload.response.JwtResponse;
import com.example.jwtdemo.payload.response.MessageResponse;
import com.example.jwtdemo.security.jwt.JwtUtils;
import com.example.jwtdemo.security.services.*;
import com.example.jwtdemo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    VerificationTokenService verificationTokenService;

    @Autowired
    PasswordResetTokenService passwordResetTokenService;

    @Autowired
    EmailService emailService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Email is already in use!"));
        }
        // Create new user's account
        User user = new User(signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                false,
                signUpRequest.isAllowExtraEmails(),
                new Timestamp(Calendar.getInstance().getTime().getTime())
                );

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleService.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleService.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleService.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleService.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userService.save(user);

        try {
            //create and save verification token
            String token = UUID.randomUUID().toString();
            verificationTokenService.save(token, user);

            //send verification email
            emailService.sendVerificationMail(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(new MessageResponse("Your account has been successfully created!"));
    }

    @PatchMapping("/updatepassword/{token}")
    public ResponseEntity<?> testPatch(@PathVariable("token") String token, @Valid @RequestBody PasswordRequest passwordRequest) {
        PasswordResetToken passwordResetToken = passwordResetTokenService.findByToken(token);

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
                User user = passwordResetTokenService.getUserByPasswordResetToken(token);
                userService.changeUserPassword(user, passwordRequest.getPassword());

                //delete passwordReset Token
                passwordResetTokenService.deleteByToken(token);
                return ResponseEntity.ok(new MessageResponse("Your password has already been changed"));
            }
        }
    }
}
