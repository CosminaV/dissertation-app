package ro.ase.ism.dissertation.dto.exam;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.validator.ValidCorrectAnswerIndex;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ValidCorrectAnswerIndex
public class ExamQuestionRequest {
    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Options list cannot be null")
    @Size(min = 3, max = 5, message = "A question must have between 3 and 5 options")
    private List<@NotBlank(message =  "Option is required") String> options;

    @NotNull(message = "Correct answer is required")
    private Integer correctAnswerIndex;

    @DecimalMin(value = "0.1", message = "Minimum points per question must be 0.1")
    private Double points;
}
