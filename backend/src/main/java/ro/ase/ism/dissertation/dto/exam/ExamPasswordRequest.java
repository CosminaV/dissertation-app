package ro.ase.ism.dissertation.dto.exam;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.validator.ValidCorrectAnswerIndex;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ValidCorrectAnswerIndex
public class ExamPasswordRequest {
    @NotBlank(message = "Exam password is required")
    private String password;
}
