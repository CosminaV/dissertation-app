package ro.ase.ism.dissertation.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.model.course.EducationLevel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourseResponse {
    private Integer courseId;
    private String courseName;
    private String academicYear;
    private EducationLevel educationLevel;
    private Integer yearOfStudy;
    private Integer semester;
    private String role; // "LECTURE" or "PRACTICAL"
    private String target; // cohort name (for lecture) or student group name (for practical)
    private Integer targetId; // id of either the courseCohort or the courseGroup
    private String teacherName;
}
