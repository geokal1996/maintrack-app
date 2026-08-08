package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.ChangePasswordRequest;
import com.codingfactory.maintrack.dto.RegisterRequest;
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
        requireUsernameAvailable(request.getUsername());

        // Kryptografoume ton kodiko PRIN ton apothikefsoume - i vasi den blepei pote to plain text.
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), hashedPassword, request.getFullName(), request.getRole());
        user.setJobTitle(request.getJobTitle());
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    // ---------------------------------------------------------------
    //  Eggrafi apo tin othoni syndesis (xoris na eisai syndedemenos)
    // ---------------------------------------------------------------

    public UserResponse register(RegisterRequest request) {
        requireUsernameAvailable(request.getUsername());

        User user = new User(
                request.getUsername().toLowerCase().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                // O ROLOS ORIZETAI EDO, APO TON SERVER - poté apo to aitima tou xristi.
                Role.TECHNICIAN
        );
        user.setJobTitle(request.getJobTitle());
        // ANENERGOS mexri na ton egkrinei epoptis. To CustomUserDetailsService
        // vazei ".disabled(!active)", opote den mporei na kanei login mexri tote.
        user.setActive(false);

        return UserResponse.from(userRepository.save(user));
    }

    // ---------------------------------------------------------------
    //  Allagi kodikou apo ton IDIO ton xristi
    // ---------------------------------------------------------------

    public void changeOwnPassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        // PROSOXI: petame IllegalArgumentException (-> 400) kai OXI BadCredentialsException
        // (-> 401). To 401 simainei "i syndesi sou den isxyei" kai to frontend to metafrazei
        // se apotomi apo syndesi. Edo omos i syndesi einai mia xara - apla ena PEDIO tis
        // formas einai lathos, pou einai 400.
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Ο τρέχων κωδικός δεν είναι σωστός");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Ο νέος κωδικός πρέπει να είναι διαφορετικός από τον τρέχοντα");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ---------------------------------------------------------------
    //  Allagi rolou se yparxonta xristi
    // ---------------------------------------------------------------

    public UserResponse updateRole(Long id, Role newRole) {
        Role requesterRole = getCurrentUserRole();
        User target = findEntityById(id);

        // Den mporeis na allaxeis ton diko sou rolo - alliws enas SUPERVISOR
        // tha mporouse na "anevasei" ton eauto tou se MANAGER.
        if (target.getUsername().equals(getCurrentUsername())) {
            throw new AccessDeniedException("Δεν μπορείς να αλλάξεις τον δικό σου ρόλο");
        }
        // Den mporeis na peiraxeis xristi pou einai isos i anoteros apo esena
        validateCanAssignRole(requesterRole, target.getRole());
        // Oute na tou dosei rolo iso i anotero apo ton diko sou
        validateCanAssignRole(requesterRole, newRole);

        target.setRole(newRole);
        return UserResponse.from(userRepository.save(target));
    }

    public void setActive(Long id, boolean active) {
        User user = findEntityById(id);
        user.setActive(active);
        userRepository.save(user);
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Δεν βρέθηκε χρήστης με id " + id));
    }

    // ---------------------------------------------------------------
    //  Voithitikes
    // ---------------------------------------------------------------

    private void requireUsernameAvailable(String username) {
        if (username != null && userRepository.findByUsername(username.toLowerCase().trim()).isPresent()) {
            throw new IllegalStateException("Το username '" + username + "' χρησιμοποιείται ήδη");
        }
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    // Dimosio: to xrisimopoiei kai to FaultService gia na katagrapsei POIOS ekane
    // tin allagi katastasis i tin anathesi.
    public User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Δεν βρέθηκε ο συνδεδεμένος χρήστης"));
    }

    // Diavazei ton rolo TOU SYNDEDEMENOU xristi (auton pou ekane to request), oxi kapoiou allou.
    // O JwtAuthenticationFilter exei idi valei to "ROLE_xxx" san authority otan epalithefsame to token.
    public Role getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> Role.valueOf(a.substring(5)))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Δεν βρέθηκε ρόλος για τον συνδεδεμένο χρήστη"));
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
                "Ο ρόλος " + requesterRole + " δεν έχει δικαίωμα για χρήστη με ρόλο " + targetRole
        );
    }
}
