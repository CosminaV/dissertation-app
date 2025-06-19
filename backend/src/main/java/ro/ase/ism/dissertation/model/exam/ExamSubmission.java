package ro.ase.ism.dissertation.model.exam;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.model.user.Student;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exam_submission", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exam_id", "student_id"})
})
public class ExamSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "grade")
    private Double grade;

    @Column(name = "score")
    private Double score;

    @Column(name = "encrypted_answers")
    private String encryptedAnswers;
}
