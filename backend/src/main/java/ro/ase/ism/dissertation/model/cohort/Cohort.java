package ro.ase.ism.dissertation.model.cohort;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.model.course.EducationLevel;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cohort", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name"})
})
public class Cohort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    @OneToMany(mappedBy = "cohort")
    private List<StudentGroup> studentGroups = new ArrayList<>();

    @OneToMany(mappedBy = "cohort")
    private List<CourseCohort> courseCohorts = new ArrayList<>();
}
