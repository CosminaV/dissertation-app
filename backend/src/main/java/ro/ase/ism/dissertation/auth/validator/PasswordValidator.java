package ro.ase.ism.dissertation.auth.validator;

import java.util.regex.Pattern;

public class PasswordValidator {

    public static boolean isPasswordValid(String password) {
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\W)(?!.*\\s).{12,}$";

        Pattern pattern = Pattern.compile(regex);

        return pattern.matcher(password).matches();
    }
}

