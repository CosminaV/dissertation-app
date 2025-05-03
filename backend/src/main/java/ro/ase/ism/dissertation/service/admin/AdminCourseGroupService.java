package ro.ase.ism.dissertation.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.ism.dissertation.dto.coursegroup.AssignCourseGroupRequest;
import ro.ase.ism.dissertation.dto.coursegroup.CourseGroupResponse;
import ro.ase.ism.dissertation.dto.coursegroup.CourseGroupTeachersResponse;
import ro.ase.ism.dissertation.exception.ConflictException;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.exception.InvalidAssignmentException;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.user.Role;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.CourseCohortRepository;
import ro.ase.ism.dissertation.repository.CourseGroupRepository;
import ro.ase.ism.dissertation.repository.CourseRepository;
import ro.ase.ism.dissertation.repository.StudentGroupRepository;
import ro.ase.ism.dissertation.repository.UserRepository;
import ro.ase.ism.dissertation.utils.FormatUtils;
import ro.ase.ism.dissertation.utils.ValidationUtils;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCourseGroupService {

    private final CourseRepository courseRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final UserRepository userRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final CourseCohortRepository courseCohortRepository;

    @Transactional
    public CourseGroupResponse assignCourseToGroup(AssignCourseGroupRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        StudentGroup studentGroup = studentGroupRepository.findById(request.getStudentGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Student group not found"));

        User practicalTeacher = userRepository.findById(request.getPracticalTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Practical teacher not found"));

        if (practicalTeacher.getRole() != Role.TEACHER) {
            throw new InvalidAssignmentException("Assigned user must be teacher");
        }

        boolean alreadyAssigned = courseGroupRepository.existsByCourseAndStudentGroupAndAcademicYear(course, studentGroup, request.getAcademicYear());
        if (alreadyAssigned) {
            throw new ConflictException("This student group is already assigned to this course in this academic year");
        }

        if (request.getAcademicYear() < ValidationUtils.getCurrentAcademicYear()) {
            throw new AccessDeniedException("Cannot assign course group to a past academic year");
        }

        log.info("Assigning course {} to group {}", course.getName(), studentGroup.getName());

        CourseGroup courseGroup = CourseGroup.builder()
                .course(course)
                .studentGroup(studentGroup)
                .practicalTeacher(practicalTeacher)
                .academicYear(request.getAcademicYear())
                .build();

        courseGroupRepository.save(courseGroup);

        return CourseGroupResponse.builder()
                .id(courseGroup.getId())
                .courseName(course.getName())
                .studentGroupName(studentGroup.getName())
                .practicalTeacherName(FormatUtils.formatFullName(practicalTeacher.getFirstName(), practicalTeacher.getLastName()))
                .academicYear(FormatUtils.formatAcademicYear(request.getAcademicYear()))
                .build();
    }

    @Transactional(readOnly = true)
    public CourseGroupTeachersResponse getTeachersForCourseGroup(Integer courseGroupId) {
        CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> new EntityNotFoundException("CourseGroup not found"));

        log.info("Getting teachers for course group {}", courseGroupId);

        User practicalTeacher = courseGroup.getPracticalTeacher();
        Course course = courseGroup.getCourse();
        StudentGroup studentGroup = courseGroup.getStudentGroup();
        Cohort cohort = studentGroup.getCohort();

        Optional<CourseCohort> courseCohortOpt = courseCohortRepository.findByCourseAndCohort(course, cohort);

        String lectureTeacherName = courseCohortOpt
                .map(cc -> FormatUtils.formatFullName(cc.getLectureTeacher().getFirstName(), cc.getLectureTeacher().getLastName()))
                .orElse("Not assigned");

        String practicalTeacherName = FormatUtils.formatFullName(practicalTeacher.getFirstName(), practicalTeacher.getLastName());

        return CourseGroupTeachersResponse.builder()
                .lectureTeacherName(lectureTeacherName)
                .practicalTeacherName(practicalTeacherName)
                .build();
    }
}
