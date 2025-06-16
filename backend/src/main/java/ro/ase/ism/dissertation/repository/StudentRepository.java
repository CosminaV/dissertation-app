package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.ase.ism.dissertation.model.user.Student;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer>, JpaSpecificationExecutor<Student> {
    @Query("""
    SELECT s FROM Student s
    WHERE s.studentGroup.cohort.id = :cohortId
""")
    List<Student> findByCohortId(@Param("cohortId") Integer cohortId);
}
