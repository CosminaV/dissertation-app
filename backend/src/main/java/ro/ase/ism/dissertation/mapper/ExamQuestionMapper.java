package ro.ase.ism.dissertation.mapper;

import org.springframework.stereotype.Component;
import ro.ase.ism.dissertation.dto.exam.ExamQuestionRequest;
import ro.ase.ism.dissertation.dto.exam.ExamQuestionResponse;
import ro.ase.ism.dissertation.model.exam.ExamQuestion;

@Component
public class ExamQuestionMapper {

    public ExamQuestion toEntity(ExamQuestionRequest request) {
        return ExamQuestion.builder()
                .questionText(request.getQuestionText())
                .options(request.getOptions())
                .correctAnswerIndex(request.getCorrectAnswerIndex())
                .points(request.getPoints())
                .build();
    }

    public ExamQuestionResponse toResponse(ExamQuestion question) {
        return ExamQuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .options(question.getOptions())
                .points(question.getPoints())
                .build();
    }
}
