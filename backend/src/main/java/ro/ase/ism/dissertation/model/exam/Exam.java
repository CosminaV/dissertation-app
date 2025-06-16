package ro.ase.ism.dissertation.model.exam;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exam")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "exam_date", nullable = false)
    private Instant examDate;

    @ManyToOne
    @JoinColumn(name = "course_cohort_id", nullable = false)
    private CourseCohort courseCohort;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    private List<ExamQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "exam")
    private List<ExamSubmission> submissions = new ArrayList<>();

    @Column(name = "password", nullable = false)
    private String password;
}
