package ro.ase.ism.dissertation.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ro.ase.ism.dissertation.model.course.EducationLevel;
import ro.ase.ism.dissertation.model.course.StudentGroup;

@Entity
@Table(name = "student")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class Student extends User {

    @Enumerated
    private EducationLevel educationLevel;

    @ManyToOne
    @JoinColumn(name = "student_group_id")
    private StudentGroup studentGroup;
}
