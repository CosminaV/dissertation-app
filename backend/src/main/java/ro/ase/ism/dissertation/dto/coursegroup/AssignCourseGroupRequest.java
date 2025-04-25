package ro.ase.ism.dissertation.dto.coursegroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.validator.ValidAcademicYear;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignCourseGroupRequest {
    private Integer courseId;
    private Integer studentGroupId;
    @ValidAcademicYear
    private Integer academicYear;
    private Integer practicalTeacherId;
}
