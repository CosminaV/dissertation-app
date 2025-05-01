package ro.ase.ism.dissertation.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.ism.dissertation.dto.coursegroup.CourseGroupResponse;
import ro.ase.ism.dissertation.dto.studentgroup.AssignStudentsToGroupRequest;
import ro.ase.ism.dissertation.dto.studentgroup.StudentGroupRequest;
import ro.ase.ism.dissertation.dto.studentgroup.StudentGroupResponse;
import ro.ase.ism.dissertation.dto.studentgroup.StudentResponse;
import ro.ase.ism.dissertation.exception.ConflictException;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.exception.StudentGroupException;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.CohortRepository;
import ro.ase.ism.dissertation.repository.CourseGroupRepository;
import ro.ase.ism.dissertation.repository.StudentGroupRepository;
import ro.ase.ism.dissertation.repository.StudentRepository;
import ro.ase.ism.dissertation.utils.FormatUtils;
import ro.ase.ism.dissertation.utils.ValidationUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStudentGroupService {

    private final CohortRepository cohortRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final StudentRepository studentRepository;

    public StudentGroupResponse createStudentGroup(StudentGroupRequest studentGroupRequest) {
        Cohort cohort = cohortRepository.findById(studentGroupRequest.getCohortId())
                .orElseThrow(() -> new EntityNotFoundException("Cohort not found"));

        if (studentGroupRepository.existsByName(studentGroupRequest.getName())) {
            throw new ConflictException("A student group with this name already exists");
        }

        ValidationUtils.validateYearsOfStudy(studentGroupRequest.getYearOfStudy(), studentGroupRequest.getEducationLevel());

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

    public List<StudentGroupResponse> getAllStudentGroups() {
        log.info("Getting all student groups");
        return studentGroupRepository.findAll().stream()
                .map(this::mapToStudentGroupResponse)
                .collect(Collectors.toList());
    }

    public StudentGroupResponse getGroupById(Integer id) {
        StudentGroup group = studentGroupRepository.findById(id)
                .orElseThrow(() -> new StudentGroupException("Group not found"));

        log.info("Getting student group by id: {}", id);

        return mapToStudentGroupResponse(group);
    }

    @Transactional
    public void assignStudentsToGroup(Integer groupId, AssignStudentsToGroupRequest request) {
        StudentGroup group = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        log.info("Assigning students to group: {}", group.getName());

        List<Student> students = studentRepository.findAllById(request.getStudentIds());

        for (Student student : students) {
            if (student.getStudentGroup() != null) {
                String existingGroup = student.getStudentGroup().getName();
                throw new ConflictException("Student is already in group " + existingGroup);
            }

            student.setStudentGroup(group);
        }

        studentRepository.saveAll(students);
    }

    @Transactional
    public void moveStudentToGroup(Integer studentId, Integer targetGroupId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentGroupException("Student not found"));

        StudentGroup newGroup = studentGroupRepository.findById(targetGroupId)
                .orElseThrow(() -> new StudentGroupException("Target group not found"));

        StudentGroup currentGroup = student.getStudentGroup();

        if (currentGroup != null && currentGroup.getId().equals(newGroup.getId())) {
            throw new StudentGroupException("Student is already in this group");
        }

        log.info("Moving student {} to group: {}", student.getEmail(), newGroup.getName());

        student.setStudentGroup(newGroup);
        studentRepository.save(student);
    }

    @Transactional
    public void removeStudentFromGroup(Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        if (student.getStudentGroup() == null) {
            throw new StudentGroupException("Student is not assigned to any group");
        }

        log.info("Removing student {} from group: {}", student.getEmail(), student.getStudentGroup().getName());

        student.setStudentGroup(null);
        studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsInGroup(Integer groupId) {
        StudentGroup group = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Student group not found"));

        log.info("Getting students in student group: {}", group.getName());

        return group.getStudents().stream()
                .map(student -> StudentResponse.builder()
                        .id(student.getId())
                        .firstName(student.getFirstName())
                        .lastName(student.getLastName())
                        .email(student.getEmail())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseGroupResponse> getCoursesForStudentGroup(Integer groupId) {
        StudentGroup group = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Student group not found"));

        log.info("Getting course groups for student group: {}", groupId);

        List<CourseGroup> courseGroups = courseGroupRepository.findByStudentGroup(group);

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
                    String academicYear = courseGroup.getAcademicYear() != null
                            ? FormatUtils.formatAcademicYear(courseGroup.getAcademicYear()) : "-";

                    return CourseGroupResponse.builder()
                            .id(courseGroup.getId())
                            .courseName(course.getName())
                            .studentGroupName(group.getName())
                            .lectureTeacherName(lectureTeacherName)
                            .practicalTeacherName(practicalTeacherName)
                            .academicYear(academicYear)
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
}
