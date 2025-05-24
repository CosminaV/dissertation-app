package ro.ase.ism.dissertation.controller.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.dto.student.StudentCourseInfoResponse;
import ro.ase.ism.dissertation.dto.student.StudentCourseResponse;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.student.StudentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<List<StudentCourseResponse>> getStudentCourses(@AuthenticationPrincipal User user) {
        log.info("Get student courses");
        return ResponseEntity.ok(studentService.getCoursesForStudent(user.getId()));
    }

    @GetMapping("/course-groups/{courseGroupId}")
    public ResponseEntity<StudentCourseInfoResponse> getCourseGroupInfo(
            @PathVariable Integer courseGroupId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(studentService.getCourseGroupInfo(courseGroupId, user.getId()));
    }

    @GetMapping("/course-cohorts/{courseCohortId}")
    public ResponseEntity<StudentCourseInfoResponse> getCourseCohortInfo(
            @PathVariable Integer courseCohortId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(studentService.getCourseCohortInfo(courseCohortId, user.getId()));
    }
}
