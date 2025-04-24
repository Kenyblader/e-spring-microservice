package com.ecommerce.user.service;

import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder; // Injecter PasswordEncoder

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        logger.info("Creating user with email: {}", user.getEmail());
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword())); // Hacher le mot de passe
            User savedUser = repository.save(user);
            logger.info("User created with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = repository.findAll();
        logger.info("Found {} users", users.size());
        return users;
    }

    public Optional<User> getUserById(Long id) {
        logger.info("Fetching user with ID: {}", id);
        return repository.findById(id);
    }
    
    public Optional<User> getUserByEmail(String email) {
    	return repository.findByEmail(email) ;
    }

    public User updateUser(Long id, User updatedUser) {
        logger.info("Updating user with ID: {}", id);
        Optional<User> existingUserOpt = repository.findById(id);
        if (!existingUserOpt.isPresent()) {
            logger.warn("User with ID {} not found", id);
            throw new RuntimeException("User not found with ID: " + id);
        }

        User existingUser = existingUserOpt.get();
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword())); // Hacher le nouveau mot de passe

        User savedUser = repository.save(existingUser);
        logger.info("User updated with ID: {}", savedUser.getId());
        return savedUser;
    }

    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);
        if (!repository.existsById(id)) {
            logger.warn("User with ID {} not found", id);
            throw new RuntimeException("User not found with ID: " + id);
        }
        repository.deleteById(id);
        logger.info("User deleted with ID: {}", id);
    }
}