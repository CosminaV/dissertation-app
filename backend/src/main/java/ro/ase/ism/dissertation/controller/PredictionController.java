package ro.ase.ism.dissertation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ro.ase.ism.dissertation.dto.exam.PredictionRecordRequest;
import ro.ase.ism.dissertation.dto.exam.PredictionRecordResponse;
import ro.ase.ism.dissertation.dto.exam.SubmissionCompletedEventResponse;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.ExamSubmissionService;
import ro.ase.ism.dissertation.service.PredictionRecordService;
import ro.ase.ism.dissertation.service.PredictionStreamService;

import java.util.List;

@RestController
@RequestMapping("/api/streaming")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionRecordService predictionRecordService;
    private final ExamSubmissionService examSubmissionService;

    @PostMapping("/predictions")
    public ResponseEntity<Void> createPredictionRecord(@Valid @RequestBody PredictionRecordRequest predictionRecordRequest) {
        predictionRecordService.createPrediction(predictionRecordRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/predictions/history")
    public ResponseEntity<List<PredictionRecordResponse>> getPredictionRecords(
            @RequestParam Integer examId
    ) {
        return ResponseEntity.ok(predictionRecordService.getPredictionHistory(examId));
    }

    @GetMapping("/submissions/history")
    public ResponseEntity<List<SubmissionCompletedEventResponse>> getSubmissionHistory(
            @RequestParam Integer examId
    ) {
        return ResponseEntity.ok(examSubmissionService.getSubmissionHistory(examId));
    }

    @CrossOrigin(origins = "https://localhost:3000")
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam Integer examId, @AuthenticationPrincipal User user) {
        return examSubmissionService.streamPredictions(examId, user.getId());
    }
}
