package ro.ase.ism.dissertation.dto.studentgroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.model.course.EducationLevel;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentGroupResponse {
    private Integer id;
    private String name;
    private Integer yearOfStudy;
    private EducationLevel educationLevel;
    private String cohortName;
    private List<StudentSummary> students;

    @Data
    @Builder
    public static class StudentSummary {
        private Integer id;
        private String firstName;
        private String lastName;
        private String email;
    }
}
