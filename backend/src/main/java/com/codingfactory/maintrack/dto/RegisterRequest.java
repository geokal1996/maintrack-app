package com.codingfactory.maintrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Aitisi eggrafis neou xristi apo tin othoni syndesis.
//
// PROSOXI: DEN yparxei pedio "role" edo, kai auto einai skopimo. An to eixе,
// opoiosdipote tha mporouse na stelnei ena aitima me role=MANAGER kai na apoktisei
// pliri dikaiomata. O rolos orizetai PANTA apo ton server (TECHNICIAN) kai o
// logariasmos dimiourgeitai ANENERGOS, mexri na ton egkrinei epoptis.
public class RegisterRequest {

    @NotBlank(message = "Το username είναι υποχρεωτικό")
    @Pattern(regexp = "^[a-z][a-z0-9._-]{2,29}$",
            message = "Το username πρέπει να έχει 3-30 χαρακτήρες, με πεζά λατινικά, "
                    + "αριθμούς, τελεία, παύλα ή κάτω παύλα (π.χ. g.kalokairinos)")
    private String username;

    @NotBlank(message = "Ο κωδικός είναι υποχρεωτικός")
    @Size(min = 8, message = "Ο κωδικός πρέπει να έχει τουλάχιστον 8 χαρακτήρες")
    @Pattern(regexp = ".*[A-Za-zΑ-Ωα-ω].*", message = "Ο κωδικός πρέπει να περιέχει τουλάχιστον ένα γράμμα")
    @Pattern(regexp = ".*\\d.*", message = "Ο κωδικός πρέπει να περιέχει τουλάχιστον έναν αριθμό")
    private String password;

    @NotBlank(message = "Το ονοματεπώνυμο είναι υποχρεωτικό")
    private String fullName;

    private String jobTitle;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
}
