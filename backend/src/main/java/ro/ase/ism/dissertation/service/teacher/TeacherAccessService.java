package ro.ase.ism.dissertation.service.teacher;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.material.Material;

@Service
@RequiredArgsConstructor
public class TeacherAccessService {

    void validateTeacherAccessToCourseGroup(CourseGroup courseGroup, Integer teacherId) {
        if (!courseGroup.getPracticalTeacher().getId().equals(teacherId)) {
            String groupName = courseGroup.getStudentGroup().getName();
            String courseName = courseGroup.getCourse().getName();
            throw new AccessDeniedException("You are not allowed to access practical materials for course '" + courseName + "' and group '" + groupName + "'");
        }
    }

    public void validateTeacherAccessToCourseCohort(CourseCohort courseCohort, Integer teacherId) {
        if (!courseCohort.getLectureTeacher().getId().equals(teacherId)) {
            String cohortName = courseCohort.getCohort().getName();
            String courseName = courseCohort.getCourse().getName();
            throw new AccessDeniedException("You are not allowed to access lecture materials for course '" + courseName + "' and cohort '" + cohortName + "'");
        }
    }

    void validateTeacherOwnsMaterial(Material material, Integer teacherId) {
        if (!material.getTeacher().getId().equals(teacherId)) {
            throw new AccessDeniedException("You are not allowed to access this material");
        }
    }
}
