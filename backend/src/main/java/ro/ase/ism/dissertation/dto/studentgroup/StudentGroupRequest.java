package ro.ase.ism.dissertation.dto.studentgroup;

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
public class StudentGroupRequest {
    @NotBlank(message = "Group name is required")
    private String name;

    @NotNull(message = "Year of study is required")
    private Integer yearOfStudy;

    @NotNull(message = "Education level is required")
    private EducationLevel educationLevel;

    private Integer cohortId;
}
