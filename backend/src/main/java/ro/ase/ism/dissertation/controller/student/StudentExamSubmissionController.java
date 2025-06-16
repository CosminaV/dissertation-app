package ro.ase.ism.dissertation.controller.student;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.dto.exam.ExamSubmissionRequest;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.ExamSubmissionService;

@RestController
@RequestMapping("/api/student/exam-submissions")
@RequiredArgsConstructor
public class StudentExamSubmissionController {

    private final ExamSubmissionService examSubmissionService;

    @PostMapping("/{submissionId}/submit")
    public ResponseEntity<Void> submitExam(
            @PathVariable Integer submissionId,
            @RequestBody ExamSubmissionRequest request,
            @AuthenticationPrincipal User user) {

        examSubmissionService.submitExam(submissionId, user.getId(), request);
        return ResponseEntity.ok().build();
    }
}
