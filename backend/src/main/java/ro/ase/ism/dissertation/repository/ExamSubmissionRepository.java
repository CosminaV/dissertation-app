package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.ism.dissertation.model.exam.ExamSubmission;

import java.util.List;
import java.util.Optional;

public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Integer> {
    boolean existsByStudentIdAndExamId(Integer studentId, Integer examId);
    Optional<ExamSubmission> findByStudentIdAndExamId(Integer studentId, Integer examId);
    List<ExamSubmission> findAllByExamId(Integer examId);
}
