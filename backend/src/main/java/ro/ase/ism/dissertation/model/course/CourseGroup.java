package ro.ase.ism.dissertation.model.course;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.model.homework.Homework;
import ro.ase.ism.dissertation.model.user.User;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "course_group",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "student_group_id", "academic_year"})
)
public class CourseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer academicYear;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "student_group_id", nullable = false)
    private StudentGroup studentGroup;

    @ManyToOne
    @JoinColumn(name = "practical_teacher_id", nullable = false)
    private User practicalTeacher;

    @OneToMany(mappedBy = "courseGroup")
    private List<Material> materials;

    @OneToMany(mappedBy = "courseGroup")
    private List<Homework> homeworks;
}
