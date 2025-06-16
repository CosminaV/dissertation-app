package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.ase.ism.dissertation.model.exam.PredictionRecord;

import java.util.List;

public interface PredictionRecordRepository extends JpaRepository<PredictionRecord, Integer> {
    @Query("""
    SELECT p FROM PredictionRecord p
    WHERE p.examSubmission.exam.id = :examId
    ORDER BY p.examSubmission.id ASC, p.windowIndex ASC
    """)
    List<PredictionRecord> findByExamIdOrderByExamSubmissionIdAscWindowIndexAsc(@Param("examId") Integer examId);
}
