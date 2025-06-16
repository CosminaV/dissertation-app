package ro.ase.ism.dissertation.controller.teacher;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.dto.exam.GradeRequest;
import ro.ase.ism.dissertation.service.ExamSubmissionService;

@RestController
@RequestMapping("/api/teacher/exam-submissions")
@RequiredArgsConstructor
public class TeacherExamSubmissionController {

    private final ExamSubmissionService examSubmissionService;

    @PatchMapping("/{submissionId}/grade")
    public ResponseEntity<Void> gradeSubmission(
            @PathVariable Integer submissionId,
            @Valid @RequestBody GradeRequest request
    ) {
        examSubmissionService.assignGrade(submissionId, request.getGrade());
        return ResponseEntity.noContent().build();
    }
}
