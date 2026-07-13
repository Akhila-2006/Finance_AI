package com.example.financeai;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    // Register
    public User register(User user) {
        return repository.save(user);
    }

    // Login
    public User login(String email, String password) {
        Optional<User> optional = repository.findByEmail(email);
        if (optional.isPresent()) {
            User dbUser = optional.get();
            if (dbUser.getPassword().equals(password)) {
                return dbUser;
            }
        }
        return null;
    }
    
    // Find by email - ADD THIS METHOD
    public User findByEmail(String email) {
        Optional<User> optional = repository.findByEmail(email);
        return optional.orElse(null);
    }
}