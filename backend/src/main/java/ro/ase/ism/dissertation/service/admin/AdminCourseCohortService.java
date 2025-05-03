package ro.ase.ism.dissertation.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.ism.dissertation.dto.coursecohort.AssignCourseCohortRequest;
import ro.ase.ism.dissertation.dto.coursecohort.CourseCohortResponse;
import ro.ase.ism.dissertation.exception.ConflictException;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.exception.InvalidAssignmentException;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.user.Role;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.CohortRepository;
import ro.ase.ism.dissertation.repository.CourseCohortRepository;
import ro.ase.ism.dissertation.repository.CourseRepository;
import ro.ase.ism.dissertation.repository.UserRepository;
import ro.ase.ism.dissertation.utils.FormatUtils;
import ro.ase.ism.dissertation.utils.ValidationUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCourseCohortService {

    private final CourseRepository courseRepository;
    private final CohortRepository cohortRepository;
    private final UserRepository userRepository;
    private final CourseCohortRepository courseCohortRepository;

    @Transactional
    public void assignLectureTeacherToCohort(AssignCourseCohortRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        Cohort cohort = cohortRepository.findById(request.getCohortId())
                .orElseThrow(() -> new EntityNotFoundException("Cohort not found"));

        User lectureTeacher = userRepository.findById(request.getLectureTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Lecture teacher not found"));

        if (lectureTeacher.getRole() != Role.TEACHER) {
            throw new InvalidAssignmentException("Assigned user must be a teacher");
        }

        boolean alreadyAssigned = courseCohortRepository.existsByCourseAndCohortAndAcademicYear(course, cohort, request.getAcademicYear());
        if (alreadyAssigned) {
            throw new ConflictException("This course is already assigned to this cohort in this academic year");
        }

        if (request.getAcademicYear() < ValidationUtils.getCurrentAcademicYear()) {
            throw new AccessDeniedException("Cannot assign course cohort to a past academic year");
        }

        log.info("Creating course cohort");

        CourseCohort courseCohort = CourseCohort.builder()
                .course(course)
                .cohort(cohort)
                .academicYear(request.getAcademicYear())
                .lectureTeacher(lectureTeacher)
                .build();

        courseCohortRepository.save(courseCohort);
    }

    @Transactional(readOnly = true)
    public List<CourseCohortResponse> getAllCourseCohorts() {
        List<CourseCohort> courseCohorts = courseCohortRepository.findAll();

        return courseCohorts.stream()
                .map(cc -> CourseCohortResponse.builder()
                        .id(cc.getId())
                        .courseId(cc.getCourse().getId())
                        .courseName(cc.getCourse().getName())
                        .cohortName(cc.getCohort().getName())
                        .lectureTeacherName(FormatUtils.formatFullName(cc.getLectureTeacher().getFirstName(), cc.getLectureTeacher().getLastName()))
                        .academicYear(FormatUtils.formatAcademicYear(cc.getAcademicYear()))
                        .build())
                .toList();
    }
}
