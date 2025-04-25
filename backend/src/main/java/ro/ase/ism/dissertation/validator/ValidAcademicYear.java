package ro.ase.ism.dissertation.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AcademicYearValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAcademicYear {
    String message() default "Academic year must be the current year or next year";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
