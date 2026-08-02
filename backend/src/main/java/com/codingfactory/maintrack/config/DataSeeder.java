package com.codingfactory.maintrack.config;

import com.codingfactory.maintrack.model.*;
import com.codingfactory.maintrack.repository.FaultRepository;
import com.codingfactory.maintrack.repository.MachineRepository;
import com.codingfactory.maintrack.repository.MaintenanceActionRepository;
import com.codingfactory.maintrack.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// To CommandLineRunner "trexei" KATHE fora pou ksekinaei i efarmogi.
// OLES oi metho doi edo einai "idempotent" - dld an ta dedomena YPARXOUN idi, den ta xanadimiourgoun.
// Etsi mporoume na to trexoume oso theloume fores xoris na dimiourgountai diploi xristes/mihanes/vlaves.
//
// PROSOXI: OLA ta onomata, mihanes kai vlaves edo einai ENTELOS FANTASTIKA - den antistoixoun
// se kanena pragmatiko xoro douleias. Ftiaxtikan mono gia na deixnei i efarmogi "zontani" se demo.
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final MachineRepository machineRepository;
    private final FaultRepository faultRepository;
    private final MaintenanceActionRepository maintenanceActionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                       MachineRepository machineRepository,
                       FaultRepository faultRepository,
                       MaintenanceActionRepository maintenanceActionRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.machineRepository = machineRepository;
        this.faultRepository = faultRepository;
        this.maintenanceActionRepository = maintenanceActionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedInitialManagerIfNoUsers();

        User supervisor = seedUserIfMissing("m.nikolaou", "Manager123!", "Μαρία Νικολάου",
                Role.SUPERVISOR, "Μηχανικός Τμήματος Μηχανολογικής Συντήρησης");
        User electrician = seedUserIfMissing("k.konstantinou", "Tech123!", "Κώστας Κωνσταντίνου",
                Role.TECHNICIAN, "Ηλεκτρολόγος Συντήρησης");
        User automation = seedUserIfMissing("d.georgiou", "Tech123!", "Δημήτρης Γεωργίου",
                Role.TECHNICIAN, "Αυτοματιστής");
        User mechanic = seedUserIfMissing("n.theodorou", "Tech123!", "Νίκος Θεοδώρου",
                Role.TECHNICIAN, "Μηχανολόγος Συντήρησης");

        Machine crl1 = seedMachineIfMissing("CRL1", "Γραμμή Ψυχρής Έλασης 1", "Έλαση");
        Machine saw1 = seedMachineIfMissing("SAW1", "Πριόνι Κοπής Ράβδων 1", "Κοπή");
        Machine fur2 = seedMachineIfMissing("FUR2", "Φούρνος Ανόπτησης 2", "Θερμική Κατεργασία");
        Machine pkg3 = seedMachineIfMissing("PKG3", "Γραμμή Συσκευασίας 3", "Συσκευασία");
        Machine cmp2 = seedMachineIfMissing("CMP2", "Συμπιεστής Αέρα 2", "Παραγωγή Πεπιεσμένου Αέρα");
        Machine elp1 = seedMachineIfMissing("ELP1", "Ηλεκτρικός Πίνακας Γενικής Διανομής 1", "Ηλεκτρολογικές Εγκαταστάσεις");

        // An gia opoiodipote logo den vrethikan/dimiourgithikan oi xristes, min prospathisoume na ftiaksoume vlaves
        // (tha petaxei NullPointerException giati oi vlaves xreiazontai "reportedBy"/"technician").
        if (supervisor == null || electrician == null || automation == null || mechanic == null) {
            log.warn("DataSeeder: den vrethikan/dimiourgithikan oloi oi xristes - paraleipetai to seeding vlavwn.");
            return;
        }

        // ---- CRL1: Γραμμή Ψυχρής Έλασης 1 (i pio "provlimatiki" mihani sto demo mas) ----
        seedFaultIfMissing(crl1, electrician,
                "Θόρυβος και δόνηση σε ρουλεμάν κυλίνδρου",
                "Έντονος θόρυβος και δόνηση κατά τη λειτουργία του κυλίνδρου έλασης. Χρειάζεται άμεσος έλεγχος.",
                FaultSeverity.CRITICAL, FaultStatus.OPEN,
                null, null, null);

        seedFaultIfMissing(crl1, mechanic,
                "Διαρροή λαδιού από υδραυλική μονάδα",
                "Παρατηρήθηκε διαρροή λαδιού στη βάση της υδραυλικής μονάδας του κυλίνδρου.",
                FaultSeverity.MEDIUM, FaultStatus.CLOSED,
                mechanic, "Αντικατάσταση τσιμούχας και συμπλήρωση λαδιού υδραυλικού συστήματος.", 45);

        seedFaultIfMissing(crl1, mechanic,
                "Φθορά τσιμούχας κυλίνδρου με διαρροή",
                "Μικρή διαρροή λαδιού λόγω φθαρμένης τσιμούχας κυλίνδρου.",
                FaultSeverity.LOW, FaultStatus.CLOSED,
                mechanic, "Αντικατάσταση τσιμούχας κυλίνδρου.", 35);

        seedFaultIfMissing(crl1, automation,
                "Σφάλμα encoder ταχύτητας κυλίνδρου",
                "Ασταθής ένδειξη ταχύτητας κυλίνδρου στο σύστημα ελέγχου.",
                FaultSeverity.MEDIUM, FaultStatus.CLOSED,
                automation, "Επανασυγχρονισμός encoder και έλεγχος καλωδίωσης.", 20);

        // ---- SAW1: Πριόνι Κοπής Ράβδων 1 ----
        seedFaultIfMissing(saw1, mechanic,
                "Φθορά λάμας κοπής",
                "Η λάμα κοπής παρουσιάζει εμφανή φθορά και ανομοιόμορφη κοπή.",
                FaultSeverity.LOW, FaultStatus.CLOSED,
                mechanic, "Αντικατάσταση λάμας κοπής και έλεγχος ευθυγράμμισης.", 20);

        seedFaultIfMissing(saw1, automation,
                "Δυσλειτουργία αισθητήρα θέσης",
                "Ο αισθητήρας θέσης δίνει ασταθή σήματα, επηρεάζοντας την ακρίβεια κοπής.",
                FaultSeverity.MEDIUM, FaultStatus.RESOLVED,
                automation, "Καθαρισμός και επανευθυγράμμιση επαγωγικού αισθητήρα θέσης.", 15);

        // ---- FUR2: Φούρνος Ανόπτησης 2 ----
        seedFaultIfMissing(fur2, electrician,
                "Απόκλιση θερμοκρασίας φούρνου εκτός ορίων",
                "Η θερμοκρασία του φούρνου αποκλίνει από τη ρυθμισμένη τιμή κατά τη λειτουργία.",
                FaultSeverity.HIGH, FaultStatus.IN_PROGRESS,
                automation, "Έλεγχος θερμοστοιχείου και καλωδίωσης· σε εξέλιξη έλεγχος προγράμματος PLC.", null);

        seedFaultIfMissing(fur2, mechanic,
                "Βλάβη σε ανεμιστήρα ψύξης",
                "Ο ανεμιστήρας ψύξης του φούρνου σταμάτησε να λειτουργεί.",
                FaultSeverity.MEDIUM, FaultStatus.CLOSED,
                mechanic, "Αντικατάσταση ανεμιστήρα ψύξης.", 60);

        // ---- PKG3: Γραμμή Συσκευασίας 3 ----
        seedFaultIfMissing(pkg3, automation,
                "Μπλοκάρισμα ιμάντα μεταφοράς",
                "Ο ιμάντας μεταφοράς μπλοκάρει κατά διαστήματα λόγω συσσώρευσης υλικού.",
                FaultSeverity.LOW, FaultStatus.CLOSED,
                mechanic, "Απεμπλοκή ιμάντα και καθαρισμός τροχαλιών.", 10);

        seedFaultIfMissing(pkg3, automation,
                "Δυσλειτουργία φωτοκύτταρου ανίχνευσης προϊόντος",
                "Το φωτοκύτταρο δεν ανιχνεύει σταθερά τα προϊόντα στη γραμμή.",
                FaultSeverity.LOW, FaultStatus.RESOLVED,
                automation, "Αντικατάσταση φωτοκύτταρου ανίχνευσης.", 15);

        // ---- CMP2: Συμπιεστής Αέρα 2 ----
        seedFaultIfMissing(cmp2, electrician,
                "Πτώση πίεσης δικτύου πεπιεσμένου αέρα",
                "Παρατηρήθηκε αισθητή πτώση πίεσης στο δίκτυο πεπιεσμένου αέρα.",
                FaultSeverity.HIGH, FaultStatus.CLOSED,
                mechanic, "Εντοπισμός και αποκατάσταση διαρροής σε σύνδεσμο σωλήνωσης.", 30);

        seedFaultIfMissing(cmp2, electrician,
                "Υπερθέρμανση κινητήρα συμπιεστή",
                "Ο κινητήρας του συμπιεστή παρουσιάζει αυξημένη θερμοκρασία λειτουργίας.",
                FaultSeverity.MEDIUM, FaultStatus.CLOSED,
                electrician, "Καθαρισμός ψυγείου και έλεγχος ανεμιστήρα ψύξης κινητήρα.", 40);

        // ---- ELP1: Ηλεκτρικός Πίνακας Γενικής Διανομής 1 ----
        seedFaultIfMissing(elp1, electrician,
                "Ενεργοποίηση θερμικού προστασίας χωρίς εμφανή αιτία",
                "Το θερμικό προστασίας ενεργοποιήθηκε χωρίς εμφανή υπερφόρτωση της γραμμής.",
                FaultSeverity.MEDIUM, FaultStatus.CLOSED,
                electrician, "Έλεγχος φορτίου γραμμής και επαναφορά θερμικού· δεν διαπιστώθηκε βλάβη.", 5);

        seedFaultIfMissing(elp1, electrician,
                "Χαλαρή σύνδεση σε ακροδέκτη γραμμής τροφοδοσίας",
                "Εντοπίστηκε χαλαρή σύνδεση σε ακροδέκτη κατά τον περιοδικό έλεγχο του πίνακα.",
                FaultSeverity.HIGH, FaultStatus.RESOLVED,
                electrician, "Σύσφιξη ακροδεκτών και θερμογράφηση πίνακα για επιβεβαίωση.", 25);

        // Afou dimiourgithikan oles oi vlaves, ipologizoume tin swsti katastasi tis kathe mihanis
        // me ton IDIO kanona pou xrisimopoiei to FaultService: DOWN an yparxei anoixti sovari vlavi,
        // UNDER_MAINTENANCE an yparxei opoiadipote alli anoixti vlavi, alliws OPERATIONAL.
        recalculateStatus(crl1);
        recalculateStatus(saw1);
        recalculateStatus(fur2);
        recalculateStatus(pkg3);
        recalculateStatus(cmp2);
        recalculateStatus(elp1);
    }

    private void seedInitialManagerIfNoUsers() {
        // An yparxei idi έστω κι ένας χρήστης, δεν κάνουμε τίποτα εδώ - αυτό τρέχει μόνο στην "άδεια" βάση.
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

    private User seedUserIfMissing(String username, String rawPassword, String fullName, Role role, String jobTitle) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User(username, passwordEncoder.encode(rawPassword), fullName, role);
            user.setJobTitle(jobTitle);
            User saved = userRepository.save(user);
            log.info("DataSeeder: dimiourgithike xristis '{}' ({})", username, role);
            return saved;
        });
    }

    private Machine seedMachineIfMissing(String code, String name, String area) {
        return machineRepository.findByCode(code).orElseGet(() -> {
            Machine machine = new Machine(code, name, area, MachineStatus.OPERATIONAL);
            Machine saved = machineRepository.save(machine);
            log.info("DataSeeder: dimiourgithike mihani '{}' - {}", code, name);
            return saved;
        });
    }

    private void seedFaultIfMissing(Machine machine, User reportedBy, String title, String description,
                                     FaultSeverity severity, FaultStatus finalStatus,
                                     User technician, String actionDescription, Integer downtimeMinutes) {
        boolean alreadyExists = faultRepository.findByMachineId(machine.getId()).stream()
                .anyMatch(f -> f.getTitle().equals(title));
        if (alreadyExists) {
            return;
        }

        Fault fault = new Fault();
        fault.setMachine(machine);
        fault.setReportedBy(reportedBy);
        fault.setTitle(title);
        fault.setDescription(description);
        fault.setSeverity(severity);
        // PROSOXI: to @PrePersist tou Fault bazei OPEN MONO an to status einai null -
        // afou to orizoume edo riti, tha meinei o telikos status pou theloume.
        fault.setStatus(finalStatus);
        if (finalStatus == FaultStatus.RESOLVED || finalStatus == FaultStatus.CLOSED) {
            fault.setResolvedAt(LocalDateTime.now());
        }
        Fault savedFault = faultRepository.save(fault);

        if (technician != null && actionDescription != null) {
            MaintenanceAction action = new MaintenanceAction();
            action.setFault(savedFault);
            action.setTechnician(technician);
            action.setDescription(actionDescription);
            action.setDowntimeMinutes(downtimeMinutes);
            maintenanceActionRepository.save(action);
        }
    }

    private void recalculateStatus(Machine machine) {
        List<Fault> faults = faultRepository.findByMachineId(machine.getId());

        boolean hasSeriousOpenFault = faults.stream().anyMatch(f ->
                (f.getStatus() == FaultStatus.OPEN || f.getStatus() == FaultStatus.IN_PROGRESS)
                        && (f.getSeverity() == FaultSeverity.HIGH || f.getSeverity() == FaultSeverity.CRITICAL));

        boolean hasAnyOpenFault = faults.stream().anyMatch(f ->
                f.getStatus() == FaultStatus.OPEN || f.getStatus() == FaultStatus.IN_PROGRESS);

        MachineStatus newStatus;
        if (hasSeriousOpenFault) {
            newStatus = MachineStatus.DOWN;
        } else if (hasAnyOpenFault) {
            newStatus = MachineStatus.UNDER_MAINTENANCE;
        } else {
            newStatus = MachineStatus.OPERATIONAL;
        }

        if (machine.getStatus() != newStatus) {
            machine.setStatus(newStatus);
            machineRepository.save(machine);
        }
    }
}
