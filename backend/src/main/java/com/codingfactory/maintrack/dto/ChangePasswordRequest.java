package com.codingfactory.maintrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Allagi kodikou apo ton IDIO ton xristi.
// Zitame kai ton PALIO kodiko epitides: an kapoios afisei tin othoni anoixti kai
// perasei allos, den prepei na mporei na "kleidosei" ton logariasmo allazontas kodiko.
public class ChangePasswordRequest {

    @NotBlank(message = "Ο τρέχων κωδικός είναι υποχρεωτικός")
    private String currentPassword;

    @NotBlank(message = "Ο νέος κωδικός είναι υποχρεωτικός")
    @Size(min = 8, message = "Ο κωδικός πρέπει να έχει τουλάχιστον 8 χαρακτήρες")
    @Pattern(regexp = ".*[A-Za-zΑ-Ωα-ω].*", message = "Ο κωδικός πρέπει να περιέχει τουλάχιστον ένα γράμμα")
    @Pattern(regexp = ".*\\d.*", message = "Ο κωδικός πρέπει να περιέχει τουλάχιστον έναν αριθμό")
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
