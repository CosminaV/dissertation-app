package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.user.User;

import java.util.List;
import java.util.Optional;

public interface CourseCohortRepository extends JpaRepository<CourseCohort, Integer> {
    boolean existsByCourseAndCohortAndAcademicYear(Course course, Cohort cohort, Integer academicYear);
    List<CourseCohort> findByLectureTeacher(User lectureTeacher);
    Optional<CourseCohort> findByCourseAndCohort(Course course, Cohort cohort);
    List<CourseCohort> findByCourse(Course course);
    @Query("SELECT DISTINCT cc.academicYear FROM CourseCohort cc WHERE cc.lectureTeacher.id = :teacherId")
    List<Integer> findAcademicYearsByLectureTeacherId(@Param("teacherId") Integer teacherId);
}
