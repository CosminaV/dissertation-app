package ro.ase.ism.dissertation.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.exception.MaterialCorruptedException;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.material.Material;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.repository.StudentEnrollmentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentAccessService {

    private final StudentEnrollmentRepository studentEnrollmentRepository;

    public void validateStudentAccessToCourseGroup(Student student, CourseGroup courseGroup) {
        StudentGroup studentGroup = courseGroup.getStudentGroup();
        Integer academicYear = courseGroup.getAcademicYear();

        boolean hasAccess = studentEnrollmentRepository.existsByStudentAndStudentGroupAndAcademicYear(student, studentGroup, academicYear);

        if (!hasAccess) {
            throw new AccessDeniedException("You are not enrolled in this course group so you don't have access");
        }
    }

    public void validateStudentAccessToCourseCohort(Student student, CourseCohort courseCohort) {
        Cohort cohort = courseCohort.getCohort();
        Integer academicYear = courseCohort.getAcademicYear();

        boolean hasAccess = studentEnrollmentRepository.existsByStudentAndCohortAndAcademicYear(student, cohort, academicYear);

        if (!hasAccess) {
            throw new AccessDeniedException("You are not enrolled in this course cohort");
        }
    }

    public void validateStudentAccessToMaterial(Student student, Material material) {
        if (material.getCourseGroup() != null) {
            validateStudentAccessToCourseGroup(student, material.getCourseGroup());
        } else if (material.getCourseCohort() != null) {
            validateStudentAccessToCourseCohort(student, material.getCourseCohort());
        } else {
            throw new MaterialCorruptedException("Material is not linked to a course group or a course cohort");
        }
    }
}
