package ro.ase.ism.dissertation.dto.coursecohort;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseCohortResponse {
    private Integer id;
    private Integer courseId;
    private String courseName;
    private String cohortName;
    private String lectureTeacherName;
    private String practicalTeacherName;
    private String academicYear;
}
