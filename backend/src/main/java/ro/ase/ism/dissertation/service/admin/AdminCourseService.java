package ro.ase.ism.dissertation.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.dto.course.CourseRequest;
import ro.ase.ism.dissertation.dto.course.CourseResponse;
import ro.ase.ism.dissertation.dto.coursecohort.CourseCohortResponse;
import ro.ase.ism.dissertation.exception.ConflictException;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.repository.CourseCohortRepository;
import ro.ase.ism.dissertation.repository.CourseRepository;
import ro.ase.ism.dissertation.utils.FormatUtils;
import ro.ase.ism.dissertation.utils.ValidationUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final CourseCohortRepository courseCohortRepository;

    public CourseResponse createCourse(CourseRequest courseRequest) {
        if (courseRepository.existsByNameAndEducationLevel(courseRequest.getName(), courseRequest.getEducationLevel())) {
            throw new ConflictException("A course with this name already exists for the same education level.");
        }

        ValidationUtils.validateYearsOfStudy(courseRequest.getYearOfStudy(), courseRequest.getEducationLevel());

        log.info("Creating new course: {}", courseRequest.getName());
        Course course = Course.builder()
                .name(courseRequest.getName())
                .yearOfStudy(courseRequest.getYearOfStudy())
                .semester(courseRequest.getSemester())
                .educationLevel(courseRequest.getEducationLevel())
                .build();

        courseRepository.save(course);

        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .yearOfStudy(course.getYearOfStudy())
                .semester(course.getSemester())
                .educationLevel(course.getEducationLevel())
                .build();
    }

    public List<CourseResponse> getAllCourses() {
        log.info("Getting all courses");
        return courseRepository.findAll().stream()
                .map(course -> CourseResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .yearOfStudy(course.getYearOfStudy())
                        .semester(course.getSemester())
                        .educationLevel(course.getEducationLevel())
                        .build())
                .collect(Collectors.toList());
    }

    public List<CourseCohortResponse> getCourseCohortsByCourseId(Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        List<CourseCohort> courseCohorts = courseCohortRepository.findByCourse(course);

        return courseCohorts.stream()
                .map(this::mapToCourseCohortResponse)
                .collect(Collectors.toList());
    }

    private CourseCohortResponse mapToCourseCohortResponse(CourseCohort cc) {
        Course course = cc.getCourse();
        Cohort cohort = cc.getCohort();

        // Get all student groups of this cohort
        List<StudentGroup> studentGroups = cohort.getStudentGroups();

        // Look for a course group matching this course and any student group in this cohort
        Optional<CourseGroup> match = studentGroups.stream()
                .flatMap(group -> group.getCourseGroups().stream())
                .filter(cg -> cg.getCourse().equals(course))
                .findFirst();

        String practicalTeacherName = match
                .map(cg -> FormatUtils.formatFullName(
                        cg.getPracticalTeacher().getFirstName(),
                        cg.getPracticalTeacher().getLastName()))
                .orElse("Not assigned");
        String lectureTeacherName = cc.getLectureTeacher() == null ? "Not assigned" :
                FormatUtils.formatFullName(
                        cc.getLectureTeacher().getFirstName(),
                        cc.getLectureTeacher().getLastName());

        return CourseCohortResponse.builder()
                .id(cc.getId())
                .courseId(course.getId())
                .courseName(course.getName())
                .cohortName(cohort.getName())
                .academicYear(FormatUtils.formatAcademicYear(cc.getAcademicYear()))
                .lectureTeacherName(lectureTeacherName)
                .practicalTeacherName(practicalTeacherName)
       .build();
    }
}
