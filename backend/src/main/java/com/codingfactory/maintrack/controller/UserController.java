package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.UserRequest;
import com.codingfactory.maintrack.dto.UserResponse;
import com.codingfactory.maintrack.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "Diaxeirisi xriston (texnikoi kai epoptes)")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @PatchMapping("/{id}/active")
    public void setActive(@PathVariable Long id, @RequestParam boolean active) {
        userService.setActive(id, active);
    }
}
