package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateUserRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        var user = new User();
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setBirthDate(request.getBirthDate());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Override
    public UserResponse getUser(UUID id) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        return toResponse(user);
    }

    @Override
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));
        return toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(), user.getName(), user.getSurname(),
            user.getBirthDate(), user.getEmail(), user.getRole());
    }
}
