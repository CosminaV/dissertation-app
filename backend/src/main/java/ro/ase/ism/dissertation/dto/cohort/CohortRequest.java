package ro.ase.ism.dissertation.dto.cohort;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.model.course.EducationLevel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CohortRequest {
    @NotBlank(message = "Cohort name is required")
    private String name;

    @NotNull(message = "Education level is required")
    private EducationLevel educationLevel;
}
