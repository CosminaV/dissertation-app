package ro.ase.ism.dissertation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ro.ase.ism.dissertation.dto.exam.ExamQuestionRequest;

public class CorrectAnswerIndexValidator implements ConstraintValidator<ValidCorrectAnswerIndex, ExamQuestionRequest> {

    @Override
    public boolean isValid(ExamQuestionRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getOptions() == null || request.getCorrectAnswerIndex() == null) {
            return true;
        }

        int index = request.getCorrectAnswerIndex();
        int size = request.getOptions().size();

        return index >= 0 && index < size;
    }
}
