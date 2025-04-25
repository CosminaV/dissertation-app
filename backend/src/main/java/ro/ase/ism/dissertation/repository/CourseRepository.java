package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.course.EducationLevel;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    boolean existsByNameAndEducationLevel(String name, EducationLevel educationLevel);
}
