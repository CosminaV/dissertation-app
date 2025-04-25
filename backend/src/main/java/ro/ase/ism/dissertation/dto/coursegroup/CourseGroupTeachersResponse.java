package ro.ase.ism.dissertation.dto.coursegroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseGroupTeachersResponse {
    private String lectureTeacherName;
    private String practicalTeacherName;
}
