package com.codingfactory.maintrack.config;

import com.codingfactory.maintrack.model.Role;
import com.codingfactory.maintrack.model.User;
import com.codingfactory.maintrack.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// To CommandLineRunner "trexei" MIA fora, kathe fora pou ksekinaei i efarmogi.
// Edo to xrisimopoioume gia na ftiaxnoume ton PROTO SUPERVISOR, giati xoris auton
// kaneis den mporei na kanei login gia na ftiaksei tous alous xristes (chicken-and-egg problem).
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // An yparxei idi έστω κι ένας χρήστης, δεν κάνουμε τίποτα - αυτό τρέχει μόνο στην "άδεια" βάση.
        if (userRepository.count() > 0) {
            return;
        }

        // O PROTOS xristis ftiaxnetai me ton PIO PSILO rolo (MANAGER), giati mono MANAGER
        // mporei na dimiourgisei SUPERVISOR-logariasmous meta - alliws menoume kollimenoi
        // (kaneis den tha mporouse na ftiaxei ton proto SUPERVISOR).
        User admin = new User(
                "admin",
                passwordEncoder.encode("Admin123!"),
                "System Administrator",
                Role.MANAGER
        );
        admin.setJobTitle("Διευθυντής Συντήρησης");
        userRepository.save(admin);

        log.warn("=====================================================================");
        log.warn(" DEN BRETHIKAN XRISTES - ftiaxtike arxikos MANAGER gia na ksekiniseis:");
        log.warn("   username: admin");
        log.warn("   password: Admin123!");
        log.warn(" Alaxe ton kodiko i sviise auton to xristi meta to proto sou login.");
        log.warn("=====================================================================");
    }
}
