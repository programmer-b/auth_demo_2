package com.dantech.auth_demo_2.controller;

import com.dantech.auth_demo_2.config.constant.Constants;
import com.dantech.auth_demo_2.model.Role;
import com.dantech.auth_demo_2.model.User;
import com.dantech.auth_demo_2.payload.LoginDto;
import com.dantech.auth_demo_2.payload.SignUpDto;
import com.dantech.auth_demo_2.repository.RoleRepository;
import com.dantech.auth_demo_2.repository.UserRepository;
import com.dantech.auth_demo_2.service.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

import org.json.JSONObject;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signin")
    public ResponseEntity<String> authenticateUser(@RequestBody LoginDto loginDto){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new ResponseEntity<>("User signed-in successfully!.", HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto){

        final String username = signUpDto.getUsername();
        final String password = signUpDto.getPassword();
        final String email = signUpDto.getEmail();

        JSONObject errorMap = null;
        String usernameError = null, emailError = null;

        // add check for username exists in a DB
        if(userRepository.existsByUsername(username)){
            usernameError = "This username already exists";
        }
        // add check for email exists in DB
        if(userRepository.existsByEmail(email)){
            emailError = "This email already exists";
        }
        if(usernameError != null || emailError != null){
            errorMap = new JSONObject();
            JSONObject errorsMap = new JSONObject();
            if(usernameError != null){
                errorsMap.put(Constants.keyUsername, usernameError);
            }
            if(emailError != null){
                errorsMap.put(Constants.keyEmail, emailError);
            }
            errorMap.put("Error", new JSONObject().put("errors" , errorsMap));
        }

        if(errorMap != null){
            return new ResponseEntity<>(errorMap.toMap(), HttpStatus.BAD_REQUEST);
        }
        // create user object
        User user = new User();
        user.setName(signUpDto.getName());
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(password));

        Role roles = roleRepository.findByName("ROLE_ADMIN").get();
        user.setRoles(Collections.singleton(roles));

        userRepository.save(user);

        JSONObject jsonString = new JSONObject()
                .put("DataPayload", new JSONObject().put("data", new JSONObject().put("token", JwtUtils.generateJwtToken(username,email, user.getId())).put("user_id", user.getId())));


        return new ResponseEntity<>(jsonString.toMap(), HttpStatus.OK);

    }
}
