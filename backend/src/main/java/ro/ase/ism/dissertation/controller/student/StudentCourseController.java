package ro.ase.ism.dissertation.controller.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.dto.exam.ExamResponse;
import ro.ase.ism.dissertation.dto.student.StudentCourseInfoResponse;
import ro.ase.ism.dissertation.dto.student.StudentCourseResponse;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.student.StudentCourseService;
import ro.ase.ism.dissertation.service.student.StudentExamService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final StudentCourseService studentCourseService;
    private final StudentExamService studentExamService;

    @GetMapping
    public ResponseEntity<List<StudentCourseResponse>> getStudentCourses(@AuthenticationPrincipal User user) {
        log.info("Get student courses");
        return ResponseEntity.ok(studentCourseService.getCoursesForStudent(user.getId()));
    }

    @GetMapping("/course-groups/{courseGroupId}")
    public ResponseEntity<StudentCourseInfoResponse> getCourseGroupInfo(
            @PathVariable Integer courseGroupId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(studentCourseService.getCourseGroupInfo(courseGroupId, user.getId()));
    }

    @GetMapping("/course-cohorts/{courseCohortId}")
    public ResponseEntity<StudentCourseInfoResponse> getCourseCohortInfo(
            @PathVariable Integer courseCohortId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(studentCourseService.getCourseCohortInfo(courseCohortId, user.getId()));
    }

    @GetMapping("/course-cohorts/{courseCohortId}/exams")
    public ResponseEntity<List<ExamResponse>> getExamsForCourseCohort(
            @PathVariable Integer courseCohortId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(studentExamService.getExamsForCourseCohort(courseCohortId, user.getId()));
    }
}
