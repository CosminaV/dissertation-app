package ro.ase.ism.dissertation.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.dto.student.StudentCourseInfoResponse;
import ro.ase.ism.dissertation.dto.student.StudentCourseResponse;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.model.user.StudentEnrollment;
import ro.ase.ism.dissertation.repository.CourseCohortRepository;
import ro.ase.ism.dissertation.repository.CourseGroupRepository;
import ro.ase.ism.dissertation.repository.StudentEnrollmentRepository;
import ro.ase.ism.dissertation.repository.StudentRepository;
import ro.ase.ism.dissertation.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final CourseCohortRepository courseCohortRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final StudentAccessService studentAccessService;

    public List<StudentCourseResponse> getCoursesForStudent(Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        log.info("Getting courses for student {}", studentId);

        // Fetch all enrollments explicitly associated with this student
        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findAllByStudent(student);

        List<StudentCourseResponse> responses = new ArrayList<>();

        for (StudentEnrollment enrollment : enrollments) {
            Integer academicYear = enrollment.getAcademicYear();
            StudentGroup studentGroup = enrollment.getStudentGroup();
            Cohort cohort = enrollment.getCohort();

            log.info("Checking student group {} and cohort {} for year {}", studentGroup, cohort, academicYear);

            // Fetch practical courses only for explicit studentGroup and year
            List<CourseGroup> practicals = courseGroupRepository
                    .findByStudentGroupAndAcademicYear(studentGroup, academicYear);

            // Fetch lectures only for explicit cohort and year
            List<CourseCohort> lectures = courseCohortRepository
                    .findByCohortAndAcademicYear(cohort, academicYear);

            // Add practical courses
            practicals.forEach(cg -> responses.add(StudentCourseResponse.builder()
                    .courseId(cg.getCourse().getId())
                    .courseName(cg.getCourse().getName())
                    .role("PRACTICAL")
                    .target(studentGroup.getName() + cohort.getName())
                    .targetId(cg.getId())
                    .teacherName(FormatUtils.formatFullName(cg.getPracticalTeacher().getFirstName(), cg.getPracticalTeacher().getLastName()))
                    .academicYear(FormatUtils.formatAcademicYear(academicYear))
                    .educationLevel(studentGroup.getEducationLevel())
                    .yearOfStudy(studentGroup.getYearOfStudy())
                    .semester(cg.getCourse().getSemester())
                    .build()));

            // Add lecture courses
            lectures.forEach(cc -> responses.add(StudentCourseResponse.builder()
                    .courseId(cc.getCourse().getId())
                    .courseName(cc.getCourse().getName())
                    .role("LECTURE")
                    .target(cohort.getName())
                    .targetId(cc.getId())
                    .teacherName(FormatUtils.formatFullName(cc.getLectureTeacher().getFirstName(), cc.getLectureTeacher().getLastName()))
                    .academicYear(FormatUtils.formatAcademicYear(academicYear))
                    .educationLevel(cohort.getEducationLevel())
                    .yearOfStudy(studentGroup.getYearOfStudy())
                    .semester(cc.getCourse().getSemester())
                    .build()));
        }

        return responses;
    }

    public StudentCourseInfoResponse getCourseGroupInfo(Integer courseGroupId, Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> new EntityNotFoundException("Course group not found"));

        studentAccessService.validateStudentAccessToCourseGroup(student, courseGroup);

        String teacherName = FormatUtils.formatFullName(
                courseGroup.getPracticalTeacher().getFirstName(),
                courseGroup.getPracticalTeacher().getLastName()
        );

        return StudentCourseInfoResponse.builder()
                .courseName(courseGroup.getCourse().getName())
                .academicYear(FormatUtils.formatAcademicYear(courseGroup.getAcademicYear()))
                .target(courseGroup.getStudentGroup().getName() + courseGroup.getStudentGroup().getCohort().getName())
                .role("PRACTICAL")
                .teacherName(teacherName)
                .build();
    }

    public StudentCourseInfoResponse getCourseCohortInfo(Integer courseCohortId, Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        CourseCohort courseCohort = courseCohortRepository.findById(courseCohortId)
                .orElseThrow(() -> new EntityNotFoundException("Course cohort not found"));

        studentAccessService.validateStudentAccessToCourseCohort(student, courseCohort);

        String teacherName = FormatUtils.formatFullName(
                courseCohort.getLectureTeacher().getFirstName(),
                courseCohort.getLectureTeacher().getLastName()
        );

        return StudentCourseInfoResponse.builder()
                .courseName(courseCohort.getCourse().getName())
                .academicYear(FormatUtils.formatAcademicYear(courseCohort.getAcademicYear()))
                .target(courseCohort.getCohort().getName())
                .role("LECTURE")
                .teacherName(teacherName)
                .build();
    }
}
