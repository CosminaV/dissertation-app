package ro.ase.ism.dissertation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.ism.dissertation.dto.exam.PredictionRecordRequest;
import ro.ase.ism.dissertation.dto.exam.PredictionRecordResponse;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.model.exam.ExamSubmission;
import ro.ase.ism.dissertation.model.exam.PredictionRecord;
import ro.ase.ism.dissertation.repository.ExamSubmissionRepository;
import ro.ase.ism.dissertation.repository.PredictionRecordRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionRecordService {

    private final PredictionRecordRepository predictionRecordRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final PredictionStreamService predictionStreamService;

    @Transactional
    public void createPrediction(PredictionRecordRequest request) {
        ExamSubmission examSubmission = examSubmissionRepository.findById(request.getExamSubmissionId())
                .orElseThrow(() -> new EntityNotFoundException("Exam submission not found"));

        PredictionRecord predictionRecord = PredictionRecord.builder()
                .examSubmission(examSubmission)
                .windowIndex(request.getWindowIndex())
                .featureVector(request.getFeatureVector())
                .predictedClass(request.getPredictedClass())
                .probabilities(request.getProbabilities())
                .timestamp(Instant.now())
                .build();
        predictionRecordRepository.save(predictionRecord);
        log.info("Prediction record {} created", predictionRecord.getId());

        Integer examId = examSubmission.getExam().getId();
        PredictionRecordResponse predictionRecordResponse = PredictionRecordResponse.builder()
                .id(predictionRecord.getId())
                .examSubmissionId(predictionRecord.getExamSubmission().getId())
                .studentId(predictionRecord.getExamSubmission().getStudent().getId())
                .windowIndex(predictionRecord.getWindowIndex())
                .predictedClass(predictionRecord.getPredictedClass())
                .probabilities(predictionRecord.getProbabilities())
                .timestamp(predictionRecord.getTimestamp())
                .build();

        predictionStreamService.publishPredictionEvent(examId, predictionRecordResponse);
    }

    public List<PredictionRecordResponse> getPredictionHistory(Integer examId) {
        log.info("Retrieving prediction history for exam {}", examId);
        return predictionRecordRepository.findByExamIdOrderByExamSubmissionIdAscWindowIndexAsc(examId)
                .stream()
                .map(predictionRecord -> PredictionRecordResponse.builder()
                        .id(predictionRecord.getId())
                        .examSubmissionId(predictionRecord.getExamSubmission().getId())
                        .studentId(predictionRecord.getExamSubmission().getStudent().getId())
                        .windowIndex(predictionRecord.getWindowIndex())
                        .predictedClass(predictionRecord.getPredictedClass())
                        .probabilities(predictionRecord.getProbabilities())
                        .timestamp(predictionRecord.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }
}
