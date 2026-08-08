package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.LoginRequest;
import com.codingfactory.maintrack.dto.LoginResponse;
import com.codingfactory.maintrack.dto.RegisterRequest;
import com.codingfactory.maintrack.dto.UserResponse;
import com.codingfactory.maintrack.exception.ResourceNotFoundException;
import com.codingfactory.maintrack.model.User;
import com.codingfactory.maintrack.repository.UserRepository;
import com.codingfactory.maintrack.security.JwtService;
import com.codingfactory.maintrack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Σύνδεση και εγγραφή χρηστών")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                           UserRepository userRepository, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Operation(summary = "Εγγραφή νέου χρήστη",
            description = "Δημόσιο endpoint. Ο λογαριασμός δημιουργείται ΠΑΝΤΑ ως Τεχνικός και "
                    + "ΑΝΕΝΕΡΓΟΣ — ο χρήστης δεν μπορεί να συνδεθεί μέχρι να τον ενεργοποιήσει "
                    + "επόπτης ή διευθυντής. Ο ρόλος δεν μπορεί να δηλωθεί από το αίτημα.")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @Operation(summary = "Σύνδεση",
            description = "Επιστρέφει JWT token που πρέπει να σταλεί σε κάθε επόμενο αίτημα "
                    + "στο header Authorization: Bearer <token>. Το token περιέχει τον ρόλο.")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // Auto to grammi kanei to "megalo" elegxo: brisko ton xristi, sygrino ton kodiko
        // (kryptografimeno) - an den tairiazei, petaei automata BadCredentialsException (401).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Δεν βρέθηκε ο χρήστης"));

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

        return new LoginResponse(user.getId(), token, user.getUsername(), user.getFullName(), user.getRole().name());
    }
}
