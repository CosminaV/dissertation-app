package ro.ase.ism.dissertation.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Duration is required")
    private Integer durationMinutes;

    @NotNull(message = "Exam date is required")
    @Future(message = "Exam date must be in the future")
    private LocalDateTime examDate;

    private Integer courseCohortId;

    @NotNull(message = "Questions list cannot be null")
    @Size(min = 1, message = "At least one question is required")
    @Valid
    private List<ExamQuestionRequest> questions;

    @NotBlank(message = "The exam password is required")
    private String password;
}
