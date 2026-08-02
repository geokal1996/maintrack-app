package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.UserRequest;
import com.codingfactory.maintrack.dto.UserResponse;
import com.codingfactory.maintrack.exception.ResourceNotFoundException;
import com.codingfactory.maintrack.model.Role;
import com.codingfactory.maintrack.model.User;
import com.codingfactory.maintrack.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        // Elenxoume oti o xristis pou kanei to aitima EXEI DIKAIOMA na dimiourgisei
        // xristi me AUTON ton rolo (p.x. enas SUPERVISOR den mporei na ftiaxei allon SUPERVISOR).
        validateCanAssignRole(getCurrentUserRole(), request.getRole());

        // Kryptografoume ton kodiko PRIN ton apothikefsoume - i vasi den blepei pote to plain text.
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), hashedPassword, request.getFullName(), request.getRole());
        user.setJobTitle(request.getJobTitle());
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

    // Diavazei ton rolo TOU SYNDEDEMENOU xristi (auton pou ekane to request), oxi kapoiou allou.
    // O JwtAuthenticationFilter exei idi valei to "ROLE_xxx" san authority otan epalithefsame to token.
    private Role getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> Role.valueOf(a.substring(5)))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Den vrethike rolos gia ton syndedemeno xristi"));
    }

    // O kanonas ierarxias: kathenas mporei na "ftiaxnei" mono xristes ME XAMILOTERO rolo apo ton eauto tou.
    // MANAGER -> mporei na ftiaxei SUPERVISOR i TECHNICIAN (oxi allon MANAGER)
    // SUPERVISOR -> mporei na ftiaxei mono TECHNICIAN
    // TECHNICIAN -> den mporei na ftiaxei kanenan (den ftanei kan edo, to SecurityConfig ton blokarei prin)
    private void validateCanAssignRole(Role requesterRole, Role targetRole) {
        if (requesterRole == Role.MANAGER && targetRole != Role.MANAGER) {
            return;
        }
        if (requesterRole == Role.SUPERVISOR && targetRole == Role.TECHNICIAN) {
            return;
        }
        throw new AccessDeniedException(
                "Ο ρόλος " + requesterRole + " δεν μπορεί να δημιουργήσει χρήστη με ρόλο " + targetRole
        );
    }
}
