package ro.ase.ism.dissertation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ro.ase.ism.dissertation.dto.exam.ExamQuestionResponse;
import ro.ase.ism.dissertation.dto.exam.ExamRequest;
import ro.ase.ism.dissertation.dto.exam.ExamResponse;
import ro.ase.ism.dissertation.exception.InvalidExamPointsException;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.exam.Exam;
import ro.ase.ism.dissertation.model.exam.ExamQuestion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExamMapper {

    private final ExamQuestionMapper examQuestionMapper;

    public Exam toEntity(ExamRequest request, CourseCohort cohort) {
        LocalDateTime localDateTime = request.getExamDate();
        ZoneId zoneId = ZoneId.of("Europe/Bucharest");
        Instant instant = localDateTime.atZone(zoneId).toInstant();
        Exam exam = Exam.builder()
                .title(request.getTitle())
                .durationMinutes(request.getDurationMinutes())
                .examDate(instant)
                .courseCohort(cohort)
                .password(request.getPassword())
                .build();

        List<ExamQuestion> questions = request.getQuestions().stream()
                .map(examQuestionMapper::toEntity)
                .peek(q -> q.setExam(exam))
                .collect(Collectors.toCollection(ArrayList::new));

        boolean anyWithPoints = questions.stream().anyMatch(q -> q.getPoints() != null);
        boolean allWithPoints = questions.stream().allMatch(q -> q.getPoints() != null);

        if (anyWithPoints && !allWithPoints) {
            throw new InvalidExamPointsException("If custom points are used, all questions must have a points value.");
        }

        if (!anyWithPoints) {
            double defaultPoints = 9.0 / questions.size();
            BigDecimal roundedPoints = BigDecimal.valueOf(defaultPoints).setScale(2, RoundingMode.HALF_UP);
            questions.forEach(q -> {
                q.setPoints(roundedPoints.doubleValue());
            });
        }

        exam.setQuestions(questions);
        return exam;
    }

    public ExamResponse toFullResponse(Exam exam) {
        return ExamResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .durationMinutes(exam.getDurationMinutes())
                .examDate(exam.getExamDate())
                .createdAt(exam.getCreatedAt())
                .courseName(exam.getCourseCohort().getCourse().getName())
                .questions(
                        exam.getQuestions() != null
                                ? exam.getQuestions().stream().map(examQuestionMapper::toResponse).toList()
                                : List.of()
                )
                .build();
    }

    public ExamResponse toSummaryResponse(Exam exam) {
        return ExamResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .examDate(exam.getExamDate())
                .build();
    }

    public ExamResponse toStudentSummaryResponse(Exam exam, boolean submitted) {
        return ExamResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .examDate(exam.getExamDate())
                .submitted(submitted)
                .build();
    }

    public ExamResponse toMetadataResponse(Exam exam) {
        return ExamResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .examDate(exam.getExamDate())
                .durationMinutes(exam.getDurationMinutes())
                .courseName(exam.getCourseCohort().getCourse().getName())
       .build();
    }

    public List<ExamQuestionResponse> toQuestionResponseList(Exam exam) {
        if (exam.getQuestions() == null) {
            return List.of();
        }
        return exam.getQuestions().stream()
                .map(examQuestionMapper::toResponse)
                .toList();
    }
}
