package com.vikpix.api.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vikpix.api.users.dto.request.CreateUserRequest;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.users.repository.UserRepository;

@Service
public class CreateUserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User execute(CreateUserRequest createUserRequest) {
        try {
            User user = User.builder()
                .name(createUserRequest.name())
                .email(createUserRequest.email())
                .password(passwordEncoder.encode(createUserRequest.password()))
                .build();

            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao registrar usuario", e);
        }
    }
}



