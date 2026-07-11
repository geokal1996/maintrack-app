package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.UserRequest;
import com.codingfactory.maintrack.dto.UserResponse;
import com.codingfactory.maintrack.exception.ResourceNotFoundException;
import com.codingfactory.maintrack.model.User;
import com.codingfactory.maintrack.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(findEntityById(id));
    }

    public UserResponse create(UserRequest request) {
        // PROSOXI: edo apothikevoume ton kodiko OS EXEI (plain text) MONO PROSORINA.
        // Sti fasi 6 (Authentication) tha ton kryptografoume me BCrypt prin apothikeftei.
        User user = new User(request.getUsername(), request.getPassword(), request.getFullName(), request.getRole());
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    public void setActive(Long id, boolean active) {
        User user = findEntityById(id);
        user.setActive(active);
        userRepository.save(user);
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }
}
