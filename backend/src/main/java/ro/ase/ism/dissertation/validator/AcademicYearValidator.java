package ro.ase.ism.dissertation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class AcademicYearValidator implements ConstraintValidator<ValidAcademicYear, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) return false;

        int currentYear = Year.now().getValue();
        return value == currentYear || value == currentYear + 1;
    }
}
