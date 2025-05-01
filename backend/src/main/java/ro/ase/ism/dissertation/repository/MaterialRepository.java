package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.material.Material;
import ro.ase.ism.dissertation.model.user.User;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Integer> {
    List<Material> findByCourseGroupAndTeacher(CourseGroup courseGroup, User teacher);
    List<Material> findByCourseCohortAndTeacher(CourseCohort courseCohort, User teacher);
}
