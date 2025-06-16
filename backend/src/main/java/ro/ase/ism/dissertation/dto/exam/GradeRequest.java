package ro.ase.ism.dissertation.dto.exam;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeRequest {
    @Min(value = 0, message = "Grade must be >= 0")
    @Max(value = 9, message = "Grade must be <= 9")
    private Double grade;
}
