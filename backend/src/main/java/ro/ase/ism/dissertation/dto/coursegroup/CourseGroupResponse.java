package ro.ase.ism.dissertation.dto.coursegroup;

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
public class CourseGroupResponse {
    private Integer id;
    private String courseName;
    private String studentGroupName;
    private String practicalTeacherName;
    private String lectureTeacherName;
    private String academicYear;
}
