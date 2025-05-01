package ro.ase.ism.dissertation.dto.coursegroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherCourseInfoResponse {
    private String courseName;
    private String academicYear;
    private String target;
    private String role;
}
