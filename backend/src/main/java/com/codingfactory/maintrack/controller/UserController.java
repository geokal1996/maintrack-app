package com.codingfactory.maintrack.controller;

import com.codingfactory.maintrack.dto.ChangePasswordRequest;
import com.codingfactory.maintrack.dto.RoleUpdateRequest;
import com.codingfactory.maintrack.dto.UserRequest;
import com.codingfactory.maintrack.dto.UserResponse;
import com.codingfactory.maintrack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "Διαχείριση χρηστών (τεχνικοί, προϊστάμενοι, διευθυντές)")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Λίστα όλων των χρηστών")
    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @Operation(summary = "Ένας χρήστης με βάση το id του")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @Operation(summary = "Δημιουργία χρήστη",
            description = "Ισχύει κανόνας ιεραρχίας: ο καθένας μπορεί να δημιουργήσει μόνο "
                    + "χρήστες με χαμηλότερο ρόλο από τον δικό του. Ο επόπτης φτιάχνει μόνο "
                    + "τεχνικούς, ο διευθυντής φτιάχνει και επόπτες.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @Operation(summary = "Ενεργοποίηση / απενεργοποίηση χρήστη",
            description = "Οι χρήστες δεν διαγράφονται ποτέ, ώστε να μη χαθεί το ιστορικό των "
                    + "βλαβών και των ενεργειών τους. Χρησιμοποιείται και για την έγκριση "
                    + "νέων εγγραφών, που δημιουργούνται ανενεργές.")
    @PatchMapping("/{id}/active")
    public void setActive(@PathVariable Long id, @RequestParam boolean active) {
        userService.setActive(id, active);
    }

    @Operation(summary = "Αλλαγή ρόλου χρήστη",
            description = "Για προαγωγές. Δεν μπορείς να αλλάξεις τον δικό σου ρόλο, ούτε να "
                    + "δώσεις ρόλο ίσο ή ανώτερο από τον δικό σου.")
    @PatchMapping("/{id}/role")
    public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return userService.updateRole(id, request.getRole());
    }

    @Operation(summary = "Αλλαγή του δικού μου κωδικού",
            description = "Απαιτεί και τον τρέχοντα κωδικό για επιβεβαίωση. Διαθέσιμο σε "
                    + "οποιονδήποτε συνδεδεμένο χρήστη, ανεξάρτητα από ρόλο.")
    @PostMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeOwnPassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changeOwnPassword(request);
    }
}
