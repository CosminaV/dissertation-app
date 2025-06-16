package ro.ase.ism.dissertation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.ism.dissertation.dto.exam.AnswerResponse;
import ro.ase.ism.dissertation.dto.exam.ExamStartResponse;
import ro.ase.ism.dissertation.dto.exam.ExamSubmissionRequest;
import ro.ase.ism.dissertation.dto.exam.ExamSubmissionResponse;
import ro.ase.ism.dissertation.dto.exam.SubmissionCompletedEventResponse;
import ro.ase.ism.dissertation.exception.ConflictException;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.exception.ExamAlreadyStartedException;
import ro.ase.ism.dissertation.exception.ExamAlreadySubmittedException;
import ro.ase.ism.dissertation.model.exam.Exam;
import ro.ase.ism.dissertation.model.exam.ExamQuestion;
import ro.ase.ism.dissertation.model.exam.ExamSubmission;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.repository.ExamQuestionRepository;
import ro.ase.ism.dissertation.repository.ExamRepository;
import ro.ase.ism.dissertation.repository.ExamSubmissionRepository;
import ro.ase.ism.dissertation.repository.StudentRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamSubmissionService {

    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final PredictionStreamService predictionStreamService;
    private final EncryptionService encryptionService;

    @Transactional
    public ExamStartResponse startExam(Integer examId, Integer studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        // prevent duplicate submission creation
        if (examSubmissionRepository.existsByStudentIdAndExamId(studentId, examId)) {
            throw new ExamAlreadyStartedException("Exam already started");
        }

        ExamSubmission submission = ExamSubmission.builder()
                .exam(exam)
                .student(student)
                .startedAt(Instant.now())
                .build();

        examSubmissionRepository.save(submission);

        return ExamStartResponse.builder()
                .submissionId(submission.getId())
                .startTime(submission.getStartedAt())
                .build();
    }

//    @Transactional
//    public void submitExam(Integer submissionId, Integer studentId, ExamSubmissionRequest request) {
//        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
//                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
//
//        if (!submission.getStudent().getId().equals(studentId)) {
//            throw new AccessDeniedException("You are not allowed to submit this exam.");
//        }
//
//        if (submission.getSubmittedAt() != null) {
//            throw new ExamAlreadySubmittedException("This exam has already been submitted.");
//        }
//
//        // store answers
//        Set<Integer> questionIds = request.getAnswers().stream()
//                .map(AnswerResponse::getQuestionId)
//                .collect(Collectors.toSet());
//
//        Map<Integer, ExamQuestion> questionsMap = examQuestionRepository.findAllById(questionIds).stream()
//                .collect(Collectors.toMap(ExamQuestion::getId, q -> q));
//
////        List<ExamAnswer> answers = request.getAnswers().stream()
////                .map(dto -> {
////                    ExamQuestion question = questionsMap.get(dto.getQuestionId());
////                    if (question == null) {
////                        throw new EntityNotFoundException("Question not found with id: " + dto.getQuestionId());
////                    }
////                    return ExamAnswer.builder()
////                            .submission(submission)
////                            .question(question)
////                            .selectedIndex(dto.getSelectedIndex())
////                            .build();
////                })
////                .collect(Collectors.toCollection(ArrayList::new));
//        List<ExamAnswer> answers = new ArrayList<>();
//        double totalScore = 0.0;
//        for (AnswerResponse answerResponse : request.getAnswers()) {
//            ExamQuestion question = questionsMap.get(answerResponse.getQuestionId());
//            if (question == null) {
//                throw new EntityNotFoundException("Question not found with id: " + answerResponse.getQuestionId());
//            }
//            boolean isCorrect = answerResponse.getSelectedIndex().equals(question.getCorrectAnswerIndex());
//            if (isCorrect) {
//                totalScore += question.getPoints();
//            }
//            ExamAnswer ans = ExamAnswer.builder()
//                    .submission(submission)
//                    .question(question)
//                    .selectedIndex(answerResponse.getSelectedIndex())
//                    .isCorrect(isCorrect)
//                    .build();
//            answers.add(ans);
//        }
//
//        // persist answers and update submission
//        examAnswerRepository.saveAll(answers);
//        submission.getExamAnswers().clear();
//        submission.getExamAnswers().addAll(answers);
//        submission.setSubmittedAt(Instant.now());
//        submission.setScore(totalScore);
//        submission.setGrade(null);
//        examSubmissionRepository.save(submission);
//
//        // trigger SSE event
//        publishSubmissionEvent(submission);
//    }

    @Transactional
    public void submitExam(Integer submissionId, Integer studentId, ExamSubmissionRequest request) {
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
        if (!submission.getStudent().getId().equals(studentId)) {
            throw new AccessDeniedException("You are not allowed to submit this exam.");
        }
        if (submission.getSubmittedAt() != null) {
            throw new ExamAlreadySubmittedException("This exam has already been submitted.");
        }

        // fetch questions
        Set<Integer> questionIds = request.getAnswers().stream()
                .map(AnswerResponse::getQuestionId)
                .collect(Collectors.toSet());
        Map<Integer, ExamQuestion> questionsMap = examQuestionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(ExamQuestion::getId, q -> q));

        // build answers + compute score
        List<AnswerResponse> answerResponses = request.getAnswers().stream()
                .map(dto -> {
                    ExamQuestion q = questionsMap.get(dto.getQuestionId());
                    if (q == null) {
                        throw new EntityNotFoundException("Question not found with id: " + dto.getQuestionId());
                    }
                    boolean correct = dto.getSelectedIndex().equals(q.getCorrectAnswerIndex());
                    return AnswerResponse.builder()
                            .questionId(q.getId())
                            .selectedIndex(dto.getSelectedIndex())
                            .isCorrect(correct)
                            .build();
                })
                .collect(Collectors.toList());

        double totalScore = answerResponses.stream()
                .filter(AnswerResponse::getIsCorrect)
                .mapToDouble(ans -> questionsMap.get(ans.getQuestionId()).getPoints())
                .sum();

        // encrypt the entire list and persist
        String cipher = encryptionService.encryptAnswers(answerResponses);
        submission.setEncryptedAnswers(cipher);
        submission.setScore(totalScore);
        submission.setGrade(null);
        submission.setSubmittedAt(Instant.now());
        examSubmissionRepository.save(submission);

        // fire SSE
        publishSubmissionEvent(submission);
    }

    @Transactional(readOnly = true)
    public ExamSubmissionResponse getSubmissionInfo(Integer examId, Integer studentId) {
        ExamSubmission submission = examSubmissionRepository.findByStudentIdAndExamId(studentId, examId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));

        return ExamSubmissionResponse.builder()
                .startedAt(submission.getStartedAt())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }

    public List<SubmissionCompletedEventResponse> getSubmissionHistory(Integer examId) {
        log.info("Retrieving submission history for exam {}", examId);
        return examSubmissionRepository.findAllByExamId(examId).stream()
                .filter(sub -> sub.getSubmittedAt() != null)
                .map(sub -> {
                    List<AnswerResponse> answers = encryptionService.decryptAnswers(sub.getEncryptedAnswers());

                    return SubmissionCompletedEventResponse.builder()
                            .examSubmissionId(sub.getId())
                            .studentId(sub.getStudent().getId())
                            .answers(answers)
                            .score(sub.getScore())
                            .grade(sub.getGrade())
                            .submittedAt(sub.getSubmittedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignGrade(Integer submissionId, Double grade) {
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));

        // verify that submission has been completed:
        if (submission.getSubmittedAt() == null) {
            throw new ConflictException("Cannot grade an unsubmitted exam");
        }

        if (submission.getGrade() != null) {
            throw new ConflictException("Grade already assigned");
        }

        submission.setGrade(grade + 1.00);
        examSubmissionRepository.save(submission);
    }

    private void publishSubmissionEvent(ExamSubmission submission) {
        List<AnswerResponse> answers = encryptionService
                .decryptAnswers(submission.getEncryptedAnswers());

        SubmissionCompletedEventResponse evt = SubmissionCompletedEventResponse.builder()
                .examSubmissionId(submission.getId())
                .studentId(submission.getStudent().getId())
                .answers(answers)
                .score(submission.getScore())
                .grade(submission.getGrade())
                .submittedAt(submission.getSubmittedAt())
                .build();

        Integer examId = submission.getExam().getId();
        predictionStreamService.publishSubmissionEvent(examId, evt);
    }

//    private void publishSubmissionEvent(ExamSubmission submission) {
//        List<AnswerResponse> answers = submission.getExamAnswers().stream()
//                .map(ans -> AnswerResponse.builder()
//                        .questionId(ans.getQuestion().getId())
//                        .selectedIndex(ans.getSelectedIndex())
//                        .isCorrect(ans.getIsCorrect())
//                        .build())
//                .toList();
//        SubmissionCompletedEventResponse submissionCompletedEventResponse = SubmissionCompletedEventResponse.builder()
//                .examSubmissionId(submission.getId())
//                .studentId(submission.getStudent().getId())
//                .answers(answers)
//                .score(submission.getScore())
//                .grade(submission.getGrade())
//                .submittedAt(submission.getSubmittedAt())
//                .build();
//
//        Integer examId = submission.getExam().getId();
//        predictionStreamService.publishSubmissionEvent(examId, submissionCompletedEventResponse);
//    }
}
