package com.codingfactory.maintrack.security;

import com.codingfactory.maintrack.model.User;
import com.codingfactory.maintrack.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// To Spring Security xreiazetai ena "UserDetailsService" gia na kserei pos na
// vrei ton xristi kai ton (kryptografimeno) kodiko tou otan kaneis login.
// Emeis tou leme "psakse ton sto diko mas UserRepository".
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Δεν βρέθηκε χρήστης με username: " + username));

        // Ftiaxnoume ena "UserDetails" (auto pou katalavainei to Spring Security)
        // apo to diko mas User entity. To role ginetai "ROLE_TECHNICIAN" i "ROLE_SUPERVISOR".
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .disabled(!user.isActive())
                .build();
    }
}
