package ro.ase.ism.dissertation.service.teacher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.ism.dissertation.dto.teacher.TeacherCourseInfoResponse;
import ro.ase.ism.dissertation.dto.teacher.TeacherCourseAssignmentResponse;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.exception.InvalidAssignmentException;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.user.Role;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.CourseCohortRepository;
import ro.ase.ism.dissertation.repository.CourseGroupRepository;
import ro.ase.ism.dissertation.repository.UserRepository;
import ro.ase.ism.dissertation.utils.FormatUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final UserRepository userRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final CourseCohortRepository courseCohortRepository;
    private final TeacherAccessService teacherAccessService;

    @Transactional(readOnly = true)
    public List<TeacherCourseAssignmentResponse> getTeacherCourses(Integer teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new InvalidAssignmentException("User is not a teacher");
        }

        log.info("Getting courses for teacher {} {}", teacher.getFirstName(), teacher.getLastName());

        List<TeacherCourseAssignmentResponse> practicalAssignments =
                courseGroupRepository.findByPracticalTeacher(teacher).stream()
                        .map(cg -> {
                            Course course = cg.getCourse();
                            StudentGroup group = cg.getStudentGroup();
                            //Cohort cohort = group.getCohort();

                            // find academic year via CourseCohort
//                            Optional<CourseCohort> ccOpt = course.getCourseCohorts().stream()
//                                    .filter(cc -> cc.getCohort().equals(cohort))
//                                    .findFirst();
//
//                            String formattedYear = ccOpt
//                                    .map(CourseCohort::getAcademicYear)
//                                    .map(FormatUtils::formatAcademicYear)
//                                    .orElse("-");

                            String formattedYear = cg.getAcademicYear() != null ? FormatUtils.formatAcademicYear(cg.getAcademicYear()) : "-";

                            return TeacherCourseAssignmentResponse.builder()
                                    .courseId(course.getId())
                                    .courseName(course.getName())
                                    .academicYear(formattedYear)
                                    .semester(course.getSemester())
                                    .educationLevel(group.getEducationLevel())
                                    .yearOfStudy(group.getYearOfStudy())
                                    .role("PRACTICAL")
                                    .target(group.getName() + group.getCohort().getName())
                                    .targetId(cg.getId())
                                    .build();
                        })
                        .toList();

        List<TeacherCourseAssignmentResponse> lectureAssignments =
                courseCohortRepository.findByLectureTeacher(teacher).stream()
                        .map(cc -> TeacherCourseAssignmentResponse.builder()
                                .courseId(cc.getCourse().getId())
                                .courseName(cc.getCourse().getName())
                                .academicYear(cc.getAcademicYear() != null ? FormatUtils.formatAcademicYear(cc.getAcademicYear()) : "-")
                                .semester(cc.getCourse().getSemester())
                                .educationLevel(cc.getCourse().getEducationLevel())
                                .yearOfStudy(cc.getCourse().getYearOfStudy())
                                .role("LECTURE")
                                .target(cc.getCohort().getName())
                                .targetId(cc.getId())
                                .build())
                        .toList();

        List<TeacherCourseAssignmentResponse> all = new ArrayList<>();
        all.addAll(lectureAssignments);
        all.addAll(practicalAssignments);
        all.sort(Comparator.comparing(TeacherCourseAssignmentResponse::getEducationLevel, Comparator.nullsLast(Enum::compareTo))
                .thenComparing(TeacherCourseAssignmentResponse::getYearOfStudy, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(TeacherCourseAssignmentResponse::getSemester, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(TeacherCourseAssignmentResponse::getCourseName, Comparator.nullsLast(String::compareTo)));

        return all;
    }

    public List<String> getTeacherAcademicYears(Integer teacherId) {
        Set<Integer> academicYears = new HashSet<>();
        List<Integer> lectureYears = courseCohortRepository.findAcademicYearsByLectureTeacherId(teacherId);
        List<Integer> practicalYears = courseGroupRepository.findAcademicYearsByPracticalTeacherId(teacherId);
        academicYears.addAll(lectureYears);
        academicYears.addAll(practicalYears);

        return academicYears
                .stream()
                .sorted(Comparator.reverseOrder()) // newest first
                .map(FormatUtils::formatAcademicYear)
                .toList();
    }

    public TeacherCourseInfoResponse getCourseGroupInfo(Integer courseGroupId, Integer teacherId) {
        CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> new EntityNotFoundException("Course group not found"));

        teacherAccessService.validateTeacherAccessToCourseGroup(courseGroup, teacherId);

        return TeacherCourseInfoResponse.builder()
                .courseName(courseGroup.getCourse().getName())
                .academicYear(FormatUtils.formatAcademicYear(courseGroup.getAcademicYear()))
                .target(courseGroup.getStudentGroup().getName() + courseGroup.getStudentGroup().getCohort().getName())
                .role("PRACTICAL")
                .build();
    }

    public TeacherCourseInfoResponse getCourseCohortInfo(Integer courseCohortId, Integer teacherId) {
        CourseCohort courseCohort = courseCohortRepository.findById(courseCohortId)
                .orElseThrow(() -> new EntityNotFoundException("Course group not found"));

        teacherAccessService.validateTeacherAccessToCourseCohort(courseCohort, teacherId);

        return TeacherCourseInfoResponse.builder()
                .courseName(courseCohort.getCourse().getName())
                .academicYear(FormatUtils.formatAcademicYear(courseCohort.getAcademicYear()))
                .target(courseCohort.getCohort().getName())
                .role("LECTURE")
                .build();
    }
}
