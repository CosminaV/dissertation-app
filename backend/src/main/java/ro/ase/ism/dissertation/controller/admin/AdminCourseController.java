package ro.ase.ism.dissertation.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.ase.ism.dissertation.dto.course.CourseRequest;
import ro.ase.ism.dissertation.dto.course.CourseResponse;
import ro.ase.ism.dissertation.dto.coursecohort.CourseCohortResponse;
import ro.ase.ism.dissertation.service.admin.AdminCourseService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseRequest courseRequest) {
        return ResponseEntity.ok(adminCourseService.createCourse(courseRequest));
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(adminCourseService.getAllCourses());
    }

    @GetMapping("/{courseId}/course-cohorts")
    public List<CourseCohortResponse> getCourseCohortsByCourseId(@PathVariable Integer courseId) {
        return adminCourseService.getCourseCohortsByCourseId(courseId);
    }
}
