package ro.ase.ism.dissertation.model.course;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "education_level"})
})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Integer yearOfStudy;

    private Integer semester;

    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    @OneToMany(mappedBy = "course")
    private List<CourseGroup> courseGroups = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    private List<CourseCohort> courseCohorts = new ArrayList<>();
}
