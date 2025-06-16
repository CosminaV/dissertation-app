package ro.ase.ism.dissertation.controller.teacher;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.dto.exam.ExamRequest;
import ro.ase.ism.dissertation.dto.exam.ExamResponse;
import ro.ase.ism.dissertation.dto.studentgroup.StudentResponse;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.teacher.TeacherExamService;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/exams")
@RequiredArgsConstructor
public class TeacherExamController {

    private final TeacherExamService teacherExamService;

    @PostMapping
    public ResponseEntity<ExamResponse> createExam(
            @Valid @RequestBody ExamRequest examRequest,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherExamService.createExam(examRequest, user.getId()));
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ExamResponse> getExamDetails(
            @PathVariable Integer examId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(teacherExamService.getExamDetails(examId, user.getId()));
    }

    @GetMapping("/{examId}/students")
    public ResponseEntity<List<StudentResponse>> getStudentsForExam(
            @PathVariable Integer examId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(teacherExamService.getStudentsForExam(examId, user.getId()));
    }
}
