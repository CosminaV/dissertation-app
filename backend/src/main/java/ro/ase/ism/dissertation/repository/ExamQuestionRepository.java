package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.ism.dissertation.model.exam.ExamQuestion;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Integer> {
}
