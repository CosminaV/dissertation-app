package ro.ase.ism.dissertation.dto.coursecohort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.validator.ValidAcademicYear;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignCourseCohortRequest {
    private Integer courseId;
    private Integer cohortId;
    @ValidAcademicYear
    private Integer academicYear;
    private Integer lectureTeacherId;
}
