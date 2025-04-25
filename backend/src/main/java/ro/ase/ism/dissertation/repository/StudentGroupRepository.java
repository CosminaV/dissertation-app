package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.ism.dissertation.model.course.StudentGroup;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Integer> {
    boolean existsByName(String name);
}
