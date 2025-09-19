package org.medical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChangePasswordDTO {
    @NotBlank
    public String currentPassword;

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9])[!-~]{8,}$",
        message = "Ο κωδικός πρέπει να έχει τουλάχιστον 8 χαρακτήρες, να περιέχει πεζά, κεφαλαία, αριθμό και ειδικό χαρακτήρα και να αποτελείται από λατινικούς χαρακτήρες ASCII (χωρίς κενά)"
    )
    public String newPassword;

    @NotBlank
    public String confirmPassword;
}
