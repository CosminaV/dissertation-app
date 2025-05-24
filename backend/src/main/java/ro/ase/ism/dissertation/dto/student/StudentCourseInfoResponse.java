package ro.ase.ism.dissertation.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourseInfoResponse {
    private String courseName;
    private String academicYear;
    private String target;
    private String role;
    private String teacherName;
}
