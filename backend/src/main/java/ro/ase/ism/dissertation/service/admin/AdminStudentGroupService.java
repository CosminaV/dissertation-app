package ro.ase.ism.dissertation.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.ism.dissertation.dto.coursegroup.CourseGroupResponse;
import ro.ase.ism.dissertation.dto.studentgroup.AssignStudentsToGroupRequest;
import ro.ase.ism.dissertation.dto.studentgroup.StudentGroupRequest;
import ro.ase.ism.dissertation.dto.studentgroup.StudentGroupResponse;
import ro.ase.ism.dissertation.exception.ConflictException;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.exception.StudentGroupException;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.course.EducationLevel;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.model.user.StudentEnrollment;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.CohortRepository;
import ro.ase.ism.dissertation.repository.CourseCohortRepository;
import ro.ase.ism.dissertation.repository.CourseGroupRepository;
import ro.ase.ism.dissertation.repository.StudentEnrollmentRepository;
import ro.ase.ism.dissertation.repository.StudentGroupRepository;
import ro.ase.ism.dissertation.repository.StudentRepository;
import ro.ase.ism.dissertation.utils.FormatUtils;
import ro.ase.ism.dissertation.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStudentGroupService {

    private final CohortRepository cohortRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final CourseCohortRepository courseCohortRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;

    @Transactional
    public StudentGroupResponse createStudentGroup(StudentGroupRequest studentGroupRequest) {
        Cohort cohort = cohortRepository.findById(studentGroupRequest.getCohortId())
                .orElseThrow(() -> new EntityNotFoundException("Cohort not found"));

        if (studentGroupRepository.existsByName(studentGroupRequest.getName())) {
            throw new ConflictException("A student group with this name already exists");
        }

        ValidationUtils.validateYearsOfStudy(studentGroupRequest.getYearOfStudy(), studentGroupRequest.getEducationLevel());

        if (!studentGroupRequest.getEducationLevel().equals(cohort.getEducationLevel())) {
            throw new ConflictException("Education level mismatch between student group and cohort");
        }

        log.info("Creating student group: {}", studentGroupRequest.getName());

        StudentGroup studentGroup = StudentGroup.builder()
                .name(studentGroupRequest.getName())
                .yearOfStudy(studentGroupRequest.getYearOfStudy())
                .educationLevel(studentGroupRequest.getEducationLevel())
                .cohort(cohort)
                .build();

        studentGroupRepository.save(studentGroup);

        return mapToStudentGroupResponse(studentGroup);
    }

    @Transactional(readOnly = true)
    public List<StudentGroupResponse> getStudentGroupsByAcademicYear(Integer academicYear) {
        if (academicYear != null) {
            log.info("Getting student groups for academic year {}", FormatUtils.formatAcademicYear(academicYear));
            List<StudentGroup> groups = studentEnrollmentRepository.findDistinctGroupsByAcademicYear(academicYear);
            if (groups.isEmpty())
            {
                log.warn("No student groups found for academic year {}", FormatUtils.formatAcademicYear(academicYear));
            }
            return groups.stream()
                    .map(group -> mapToStudentGroupResponse(group, academicYear))
                    .toList();
        } else {
            log.info("Getting all current student groups");
            return studentGroupRepository.findAll().stream()
                    .map(this::mapToStudentGroupResponse)
                    .toList();
        }
    }

    @Transactional(readOnly = true)
    public List<String> getAllAcademicYears() {
        log.info("Getting all academic years ");

        return studentEnrollmentRepository.findDistinctAcademicYears()
                .stream()
                .sorted(Comparator.reverseOrder())
                .map(FormatUtils::formatAcademicYear)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentGroupResponse getStudentGroupByIdAndAcademicYear(Integer groupId, Integer academicYear) {
        StudentGroup group = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new StudentGroupException("Group not found"));

        if (academicYear != null) {
            boolean hasEnrollments = studentEnrollmentRepository.existsByStudentGroupAndAcademicYear(group, academicYear);
            if (hasEnrollments) {
                log.info("Getting details about student group {} from academic year {}", group.getName(), FormatUtils.formatAcademicYear(academicYear));
                return mapToStudentGroupResponse(group, academicYear);
            } else {
                throw new EntityNotFoundException("No enrollment data found for student group " + group.getName() + " in academic year " + academicYear);
            }
        } else {
            log.info("Getting info about student group {}", group.getName());
            return mapToStudentGroupResponse(group);
        }
    }

    @Transactional
    public void assignStudentsToGroup(Integer groupId, AssignStudentsToGroupRequest request) {
        StudentGroup group = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        EducationLevel groupLevel = group.getEducationLevel();
        Integer groupYear = group.getYearOfStudy();
        Cohort cohort = cohortRepository.findById(group.getCohort().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Cohort not found"));

        log.info("Assigning students to group: {}", group.getName());

        List<Student> students = studentRepository.findAllById(request.getStudentIds());
        List<StudentEnrollment> studentEnrollments = new ArrayList<>();

        for (Student student : students) {
            if (student.getStudentGroup() != null) {
                String existingGroup = student.getStudentGroup().getName();
                throw new ConflictException("Student is already in group " + existingGroup);
            }
            if (!groupLevel.equals(student.getEducationLevel())) {
                throw new ConflictException("Education level mismatch between student and student group");
            }

            student.setStudentGroup(group);

            int currentAcademicYear = ValidationUtils.getCurrentAcademicYear();
            if (studentEnrollmentRepository.existsByStudentAndAcademicYear(student, currentAcademicYear)) {
                throw new ConflictException("Student is already enrolled for the current academic year " + currentAcademicYear);
            }

            StudentEnrollment studentEnrollment = StudentEnrollment.builder()
                    .student(student)
                    .academicYear(ValidationUtils.getCurrentAcademicYear())
                    .educationLevel(groupLevel)
                    .yearOfStudy(groupYear)
                    .cohort(cohort)
                    .studentGroup(group)
                    .build();
            studentEnrollments.add(studentEnrollment);
        }

        studentRepository.saveAll(students);
        studentEnrollmentRepository.saveAll(studentEnrollments);
    }

    @Transactional
    public void moveStudentToGroup(Integer studentId, Integer targetGroupId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentGroupException("Student not found"));

        StudentGroup targetGroup = studentGroupRepository.findById(targetGroupId)
                .orElseThrow(() -> new StudentGroupException("Target group not found"));

        StudentGroup currentGroup = student.getStudentGroup();

        int currentAcademicYear = ValidationUtils.getCurrentAcademicYear();

        StudentEnrollment studentEnrollment = studentEnrollmentRepository
                .findByStudentAndAcademicYear(student, currentAcademicYear)
                .orElseThrow(() -> new EntityNotFoundException("Student does not have an enrollment record"));

        validateMoveConditions(student, currentGroup, targetGroup, studentEnrollment);

        log.info("Moving student {} to group: {}", student.getEmail(), targetGroup.getName());
        student.setStudentGroup(targetGroup);
        studentRepository.save(student);

        log.info("Updating the student enrollment for student {}", studentEnrollment.getStudent().getEmail());
        studentEnrollment.setStudentGroup(targetGroup);
        studentEnrollment.setCohort(targetGroup.getCohort());
        studentEnrollmentRepository.save(studentEnrollment);
    }

    @Transactional
    public void removeStudentFromGroup(Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        int currentAcademicYear = ValidationUtils.getCurrentAcademicYear();

        // find enrollment for the current academic year
        StudentEnrollment enrollment = studentEnrollmentRepository
                .findByStudentAndAcademicYear(student, currentAcademicYear)
                .orElseThrow(() -> new StudentGroupException("Student is not enrolled for the current academic year"));

        log.info("Removing student {} from group: {} (academic year {})",
                student.getEmail(), enrollment.getStudentGroup().getName(), currentAcademicYear);

        // delete enrollment
        studentEnrollmentRepository.delete(enrollment);

        // ff student's current group matches the enrollment group => clear it
        if (student.getStudentGroup() != null &&
                student.getStudentGroup().getId().equals(enrollment.getStudentGroup().getId())) {
            student.setStudentGroup(null);
            studentRepository.save(student);
        }
    }

    @Transactional(readOnly = true)
    public List<CourseGroupResponse> getCoursesForStudentGroup(Integer groupId, Integer academicYear) {
        StudentGroup group = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Student group not found"));

        log.info("Getting course groups for student group: {}", groupId);

        List<CourseGroup> courseGroups = (academicYear != null)
                ? courseGroupRepository.findByStudentGroupAndAcademicYear(group, academicYear)
                : courseGroupRepository.findByStudentGroup(group);

        return courseGroups.stream()
                .map(courseGroup -> {
                    Course course = courseGroup.getCourse();
                    User practicalTeacher = courseGroup.getPracticalTeacher();

                    // find lecture teacher from courseCohort
                    Cohort cohort = group.getCohort();
                    Optional<CourseCohort> courseCohortOpt = course.getCourseCohorts().stream()
                            .filter(courseCohort -> courseCohort.getCohort().equals(cohort))
                            .findFirst();

                    String lectureTeacherName = courseCohortOpt
                            .map(cc -> FormatUtils.formatFullName(cc.getLectureTeacher().getFirstName(), cc.getLectureTeacher().getLastName()))
                            .orElse("Not assigned");
                    String practicalTeacherName = FormatUtils.formatFullName(practicalTeacher.getFirstName(), practicalTeacher.getLastName());
                    String formattedAcademicYear = courseGroup.getAcademicYear() != null
                            ? FormatUtils.formatAcademicYear(courseGroup.getAcademicYear()) : "-";

                    return CourseGroupResponse.builder()
                            .id(courseGroup.getId())
                            .courseName(course.getName())
                            .studentGroupName(group.getName())
                            .lectureTeacherName(lectureTeacherName)
                            .practicalTeacherName(practicalTeacherName)
                            .academicYear(formattedAcademicYear)
                            .build();
                })
                .toList();
    }

    private StudentGroupResponse mapToStudentGroupResponse(StudentGroup studentGroup) {
        List<StudentGroupResponse.StudentSummary> studentSummaryList = studentGroup.getStudents() == null ? List.of()
                : studentGroup.getStudents().stream()
                .map(user -> StudentGroupResponse.StudentSummary.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());

        return StudentGroupResponse.builder()
                .id(studentGroup.getId())
                .name(studentGroup.getName())
                .yearOfStudy(studentGroup.getYearOfStudy())
                .educationLevel(studentGroup.getEducationLevel())
                .cohortName(studentGroup.getCohort().getName())
                .students(studentSummaryList)
                .build();
    }

    public StudentGroupResponse mapToStudentGroupResponse(StudentGroup group, Integer academicYear) {
        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByStudentGroupIdAndAcademicYear(group.getId(), academicYear);

        List<StudentGroupResponse.StudentSummary> studentSummaryList = enrollments.stream()
                .map(enr -> {
                    Student student = enr.getStudent();
                    return StudentGroupResponse.StudentSummary.builder()
                            .id(student.getId())
                            .firstName(student.getFirstName())
                            .lastName(student.getLastName())
                            .email(student.getEmail())
                            .build();
                })
                .toList();

        return StudentGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .yearOfStudy(group.getYearOfStudy())
                .educationLevel(group.getEducationLevel())
                .cohortName(group.getCohort().getName())
                .academicYear(FormatUtils.formatAcademicYear(academicYear))
                .students(studentSummaryList)
                .build();
    }

    private void validateMoveConditions(Student student, StudentGroup currentGroup, StudentGroup targetGroup, StudentEnrollment studentEnrollment) {
        if (currentGroup == null) {
            throw new StudentGroupException("Student is not currently assigned to any group");
        }
        if (!targetGroup.getEducationLevel().equals(student.getEducationLevel())) {
            throw new ConflictException("Education level mismatch between student and target student group");
        }
        if (currentGroup.getId().equals(targetGroup.getId())) {
            throw new StudentGroupException("Student is already in this group");
        }
        if (!currentGroup.getEducationLevel().equals(targetGroup.getEducationLevel())) {
            throw new ConflictException("Education level mismatch between current and target student group");
        }
        if (!Objects.equals(currentGroup.getYearOfStudy(), targetGroup.getYearOfStudy())) {
            throw new ConflictException("Cannot move student to a group with a different year of study");
        }

        ValidationUtils.validateAcademicYearUpdates(studentEnrollment.getAcademicYear(), "Student can only be moved within current academic year");

        boolean hasCourseGroups = courseGroupRepository.existsByStudentGroupAndAcademicYear(currentGroup, studentEnrollment.getAcademicYear())
                || courseGroupRepository.existsByStudentGroupAndAcademicYear(targetGroup, studentEnrollment.getAcademicYear());
        boolean hasCourseCohorts = courseCohortRepository.existsByCohortAndAcademicYear(currentGroup.getCohort(), studentEnrollment.getAcademicYear())
                || courseCohortRepository.existsByCohortAndAcademicYear(targetGroup.getCohort(), studentEnrollment.getAcademicYear());
        if (hasCourseGroups || hasCourseCohorts) {
            throw new ConflictException("Cannot move student: course groups or course cohorts already exist");
        }
    }
}
