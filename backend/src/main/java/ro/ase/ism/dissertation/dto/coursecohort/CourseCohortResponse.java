package ro.ase.ism.dissertation.dto.coursecohort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseCohortResponse {
    private Integer id;
    private Integer courseId;
    private String courseName;
    private String cohortName;
    private String lectureTeacherName;
    private String practicalTeacherName;
    private String academicYear;
}
