package ro.ase.ism.dissertation.controller.teacher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.dto.teacher.TeacherCourseAssignmentResponse;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.TeacherService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/teacher/courses")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final TeacherService teacherService;

    @GetMapping
    public ResponseEntity<List<TeacherCourseAssignmentResponse>> getTeacherCourses(@AuthenticationPrincipal User user) {
        log.info("Get teacher courses");
        return ResponseEntity.ok(teacherService.getTeacherCourses(user.getId()));
    }

    @GetMapping("/academic-years")
    public ResponseEntity<List<String>> getAcademicYears(@AuthenticationPrincipal User user) {
        log.info("Get academic years");
        return ResponseEntity.ok(teacherService.getTeacherAcademicYears(user.getId()));
    }
}
