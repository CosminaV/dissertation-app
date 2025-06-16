package ro.ase.ism.dissertation.controller.teacher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.dto.exam.ExamResponse;
import ro.ase.ism.dissertation.dto.teacher.TeacherCourseInfoResponse;
import ro.ase.ism.dissertation.dto.teacher.TeacherCourseAssignmentResponse;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.teacher.TeacherExamService;
import ro.ase.ism.dissertation.service.teacher.TeacherCourseService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/teacher/courses")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final TeacherCourseService teacherCourseService;
    private final TeacherExamService teacherExamService;

    @GetMapping
    public ResponseEntity<List<TeacherCourseAssignmentResponse>> getTeacherCourses(@AuthenticationPrincipal User user) {
        log.info("Get teacher courses");
        return ResponseEntity.ok(teacherCourseService.getTeacherCourses(user.getId()));
    }

    @GetMapping("/academic-years")
    public ResponseEntity<List<String>> getAcademicYears(@AuthenticationPrincipal User user) {
        log.info("Get academic years");
        return ResponseEntity.ok(teacherCourseService.getTeacherAcademicYears(user.getId()));
    }

    @GetMapping("/course-groups/{courseGroupId}")
    public ResponseEntity<TeacherCourseInfoResponse> getCourseGroupInfo(
            @PathVariable Integer courseGroupId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(teacherCourseService.getCourseGroupInfo(courseGroupId, user.getId()));
    }

    @GetMapping("/course-cohorts/{courseCohortId}")
    public ResponseEntity<TeacherCourseInfoResponse> getCourseCohortInfo(
            @PathVariable Integer courseCohortId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(teacherCourseService.getCourseCohortInfo(courseCohortId, user.getId()));
    }

    @GetMapping("/course-cohorts/{courseCohortId}/exams")
    public ResponseEntity<List<ExamResponse>> getExamsForCourseCohort(
            @PathVariable Integer courseCohortId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(teacherExamService.getExamsForCourseCohort(courseCohortId, user.getId()));
    }
}
