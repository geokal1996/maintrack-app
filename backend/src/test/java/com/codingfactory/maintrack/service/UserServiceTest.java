package com.codingfactory.maintrack.service;

import com.codingfactory.maintrack.dto.UserRequest;
import com.codingfactory.maintrack.dto.UserResponse;
import com.codingfactory.maintrack.model.Role;
import com.codingfactory.maintrack.model.User;
import com.codingfactory.maintrack.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Elenxei ton NEO kanona ierarxias rolon: "kathenas mporei na ftiaxnei mono xristes
// ME XAMILOTERO rolo apo ton eauto tou" (MANAGER -> SUPERVISOR/TECHNICIAN, SUPERVISOR -> mono TECHNICIAN).
//
// Gia na "prospoiithoume" oti kapoios sygkekrimenos rolos einai syndedemenos, vazoume
// mia dokimastiki Authentication mesa sto SecurityContextHolder prin apo kathe test -
// akrivos etsi diavazei to UserService ton rolo tou syndedemenou xristi.
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void katharismosSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void sindeseSanRolo(Role role) {
        var authentication = new TestingAuthenticationToken(
                "dokimastikos-xristis",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private UserRequest ftiakseRequest(Role targetRole) {
        UserRequest request = new UserRequest();
        request.setUsername("neos.xristis");
        request.setPassword("Test1234!");
        request.setFullName("Neos Xristis");
        request.setRole(targetRole);
        return request;
    }

    @Test
    void oManagerBoreiNaFtiaxeiSupervisor() {
        sindeseSanRolo(Role.MANAGER);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.create(ftiakseRequest(Role.SUPERVISOR));

        assertThat(response.getRole()).isEqualTo(Role.SUPERVISOR);
    }

    @Test
    void oManagerBoreiNaFtiaxeiTechnician() {
        sindeseSanRolo(Role.MANAGER);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.create(ftiakseRequest(Role.TECHNICIAN));

        assertThat(response.getRole()).isEqualTo(Role.TECHNICIAN);
    }

    @Test
    void oManagerDenBoreiNaFtiaxeiAllonManager() {
        sindeseSanRolo(Role.MANAGER);

        assertThrows(AccessDeniedException.class,
                () -> userService.create(ftiakseRequest(Role.MANAGER)));

        verify(userRepository, never()).save(any());
    }

    @Test
    void oSupervisorBoreiNaFtiaxeiMonoTechnician() {
        sindeseSanRolo(Role.SUPERVISOR);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.create(ftiakseRequest(Role.TECHNICIAN));

        assertThat(response.getRole()).isEqualTo(Role.TECHNICIAN);
    }

    @Test
    void oSupervisorDenBoreiNaFtiaxeiAllonSupervisor() {
        sindeseSanRolo(Role.SUPERVISOR);

        assertThrows(AccessDeniedException.class,
                () -> userService.create(ftiakseRequest(Role.SUPERVISOR)));

        verify(userRepository, never()).save(any());
    }

    @Test
    void oSupervisorDenBoreiNaFtiaxeiManager() {
        sindeseSanRolo(Role.SUPERVISOR);

        assertThrows(AccessDeniedException.class,
                () -> userService.create(ftiakseRequest(Role.MANAGER)));

        verify(userRepository, never()).save(any());
    }
}
