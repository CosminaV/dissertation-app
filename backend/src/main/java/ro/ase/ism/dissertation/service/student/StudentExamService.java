package ro.ase.ism.dissertation.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.ism.dissertation.dto.exam.ExamQuestionResponse;
import ro.ase.ism.dissertation.dto.exam.ExamResponse;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.exception.InvalidExamPasswordException;
import ro.ase.ism.dissertation.mapper.ExamMapper;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.exam.Exam;
import ro.ase.ism.dissertation.model.exam.ExamSubmission;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.repository.CourseCohortRepository;
import ro.ase.ism.dissertation.repository.ExamRepository;
import ro.ase.ism.dissertation.repository.ExamSubmissionRepository;
import ro.ase.ism.dissertation.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentExamService {

    private final CourseCohortRepository courseCohortRepository;
    private final StudentRepository studentRepository;
    private final StudentAccessService studentAccessService;
    private final ExamRepository examRepository;
    private final ExamMapper examMapper;
    private final ExamSubmissionRepository examSubmissionRepository;

    @Transactional(readOnly = true)
    public ExamResponse getExamDetails(Integer examId, Integer studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        CourseCohort courseCohort = exam.getCourseCohort();
        studentAccessService.validateStudentAccessToCourseCohort(student, courseCohort);

        return examMapper.toMetadataResponse(exam);
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getExamsForCourseCohort(Integer courseCohortId, Integer studentId) {
        CourseCohort courseCohort = courseCohortRepository.findById(courseCohortId)
                .orElseThrow(() -> new EntityNotFoundException("Course cohort not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        studentAccessService.validateStudentAccessToCourseCohort(student, courseCohort);

        List<Exam> exams = examRepository.findByCourseCohort(courseCohort);
        return exams.stream()
                .map(exam -> {
                    Optional<ExamSubmission> submission = examSubmissionRepository.findByStudentIdAndExamId(studentId, exam.getId());
                    boolean submitted = submission.isPresent() && submission.get().getSubmittedAt() != null;
                    return examMapper.toStudentSummaryResponse(exam, submitted);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public void verifyExamPassword(Integer examId, String password, Integer studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        studentAccessService.validateStudentAccessToCourseCohort(student, exam.getCourseCohort());

        if (!exam.getPassword().equals(password)) {
            throw new InvalidExamPasswordException("Invalid exam password");
        }
    }

    @Transactional(readOnly = true)
    public List<ExamQuestionResponse> getQuestionsForExam(Integer examId, Integer studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        studentAccessService.validateStudentAccessToCourseCohort(student, exam.getCourseCohort());

        return examMapper.toQuestionResponseList(exam);
    }
}
