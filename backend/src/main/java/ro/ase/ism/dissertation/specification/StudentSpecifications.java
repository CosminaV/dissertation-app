package ro.ase.ism.dissertation.specification;

import org.springframework.data.jpa.domain.Specification;
import ro.ase.ism.dissertation.model.course.EducationLevel;
import ro.ase.ism.dissertation.model.user.Student;

public class StudentSpecifications {

    public static Specification<Student> hasNoStudentGroup() {
        return (root, query, cb) -> cb.isNull(root.get("studentGroup"));
    }

    public static Specification<Student> hasCohortId(Integer cohortId) {
        return (root, query, cb) ->
                cb.equal(root.get("studentGroup").get("cohort").get("id"), cohortId);
    }

    public static Specification<Student> hasEducationLevel(EducationLevel level) {
        return (root, query, cb) ->
                cb.equal(root.get("educationLevel"), level);
    }
}
