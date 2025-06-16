package ro.ase.ism.dissertation.model.coursecohort;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.exam.Exam;
import ro.ase.ism.dissertation.model.material.Material;
import ro.ase.ism.dissertation.model.user.User;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_cohort",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "cohort_id", "academic_year"}))
public class CourseCohort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer academicYear;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @ManyToOne
    @JoinColumn(name = "lecture_teacher_id", nullable = false)
    private User lectureTeacher;

    @OneToMany(mappedBy = "courseCohort")
    private List<Material> materials;

    @OneToMany(mappedBy = "courseCohort")
    private List<Exam> exams;
}
