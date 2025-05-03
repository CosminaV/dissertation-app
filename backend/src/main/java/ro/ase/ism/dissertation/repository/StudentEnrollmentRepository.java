package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.model.user.StudentEnrollment;

import java.util.List;
import java.util.Optional;

public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Integer> {
    Optional<StudentEnrollment> findByStudentAndAcademicYear(Student student, Integer academicYear);
    boolean existsByStudentAndStudentGroupAndAcademicYear(Student student, StudentGroup studentGroup, Integer academicYear);
    boolean existsByStudentAndCohortAndAcademicYear(Student student, Cohort course, Integer academicYear);
    List<StudentEnrollment> findAllByStudent(Student student);
    @Query("SELECT DISTINCT se.academicYear FROM StudentEnrollment se")
    List<Integer> findDistinctAcademicYears();
    @Query("SELECT DISTINCT se.studentGroup FROM StudentEnrollment se WHERE se.academicYear= :academicYear")
    List<StudentGroup> findDistinctGroupsByAcademicYear(@Param("academicYear") Integer academicYear);
    @Query("SELECT se FROM StudentEnrollment se WHERE se.studentGroup.id = :groupId AND se.academicYear = :year")
    List<StudentEnrollment> findByStudentGroupIdAndAcademicYear(@Param("groupId") Integer groupId, @Param("year") Integer year);
    boolean existsByStudentGroupAndAcademicYear(StudentGroup studentGroup, Integer academicYear);
    boolean existsByStudentAndAcademicYear(Student student, Integer academicYear);
}
