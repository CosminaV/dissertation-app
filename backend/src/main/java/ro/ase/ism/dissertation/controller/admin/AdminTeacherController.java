package ro.ase.ism.dissertation.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.dto.teacher.TeacherCourseAssignmentResponse;
import ro.ase.ism.dissertation.service.teacher.TeacherCourseService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
public class AdminTeacherController {

    private final TeacherCourseService teacherCourseService;

    @GetMapping("/{teacherId}/courses")
    public ResponseEntity<List<TeacherCourseAssignmentResponse>> getTeacherCourses(@PathVariable Integer teacherId) {
        List<TeacherCourseAssignmentResponse> courses = teacherCourseService.getTeacherCourses(teacherId);
        return ResponseEntity.ok(courses);
    }
}
