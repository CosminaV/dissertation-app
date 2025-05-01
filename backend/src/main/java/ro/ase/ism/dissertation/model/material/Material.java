package ro.ase.ism.dissertation.model.material;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.user.User;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String filePath;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    @UpdateTimestamp
    @Column(nullable = false, name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @ManyToOne
    @JoinColumn(name = "course_group_id")
    private CourseGroup courseGroup;

    @ManyToOne
    @JoinColumn(name = "course_cohort_id")
    private CourseCohort courseCohort;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    private String originalFileName;
    private String contentType;
}
