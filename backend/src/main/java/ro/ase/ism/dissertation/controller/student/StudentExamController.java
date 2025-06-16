package ro.ase.ism.dissertation.controller.student;

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
import ro.ase.ism.dissertation.dto.exam.ExamPasswordRequest;
import ro.ase.ism.dissertation.dto.exam.ExamQuestionResponse;
import ro.ase.ism.dissertation.dto.exam.ExamResponse;
import ro.ase.ism.dissertation.dto.exam.ExamStartResponse;
import ro.ase.ism.dissertation.dto.exam.ExamSubmissionResponse;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.ExamSubmissionService;
import ro.ase.ism.dissertation.service.student.StudentExamService;

import java.util.List;

@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
public class StudentExamController {

    private final StudentExamService studentExamService;
    private final ExamSubmissionService examSubmissionService;

    @GetMapping("/{examId}")
    public ResponseEntity<ExamResponse> getExamDetails(
            @PathVariable Integer examId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(studentExamService.getExamDetails(examId, user.getId()));
    }

    @PostMapping("/{examId}/verify-password")
    public ResponseEntity<Void> verifyExamPassword(
            @PathVariable Integer examId,
            @RequestBody ExamPasswordRequest request,
            @AuthenticationPrincipal User user
    ) {
        studentExamService.verifyExamPassword(examId, request.getPassword(), user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{examId}/start")
    public ResponseEntity<ExamStartResponse> startExam(
            @PathVariable Integer examId,
            @AuthenticationPrincipal User user) {

        ExamStartResponse response = examSubmissionService.startExam(examId, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{examId}/questions")
    public ResponseEntity<List<ExamQuestionResponse>> getExamQuestions(
            @PathVariable Integer examId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(studentExamService.getQuestionsForExam(examId, user.getId()));
    }

    @GetMapping("/{examId}/submission-info")
    public ResponseEntity<ExamSubmissionResponse> getExamSubmissionInfo(
            @PathVariable Integer examId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(examSubmissionService.getSubmissionInfo(examId, user.getId()));
    }
}
