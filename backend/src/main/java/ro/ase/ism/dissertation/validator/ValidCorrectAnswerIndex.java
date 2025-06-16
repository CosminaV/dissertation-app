package ro.ase.ism.dissertation.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CorrectAnswerIndexValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCorrectAnswerIndex {
    String message() default "Correct answer index must be within the range of options";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default{};
}
