package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.user.User;

import java.util.List;

public interface CourseGroupRepository extends JpaRepository<CourseGroup, Integer> {
    boolean existsByCourseAndStudentGroupAndAcademicYear(Course course, StudentGroup studentGroup, Integer academicYear);
    List<CourseGroup> findByStudentGroup(StudentGroup studentGroup);
    List<CourseGroup> findByPracticalTeacher(User practicalTeacher);
    @Query("SELECT DISTINCT cg.academicYear FROM CourseGroup cg WHERE cg.practicalTeacher.id = :teacherId")
    List<Integer> findAcademicYearsByPracticalTeacherId(@Param("teacherId")Integer teacherId);
    boolean existsByStudentGroupAndAcademicYear(StudentGroup studentGroup, Integer academicYear);
    List<CourseGroup> findByStudentGroupAndAcademicYear(StudentGroup studentGroup, Integer academicYear);
}
