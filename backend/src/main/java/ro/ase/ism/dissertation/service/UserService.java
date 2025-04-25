package ro.ase.ism.dissertation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.auth.dto.UserDTO;
import ro.ase.ism.dissertation.dto.studentgroup.StudentResponse;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.model.course.EducationLevel;
import ro.ase.ism.dissertation.model.user.Role;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.StudentRepository;
import ro.ase.ism.dissertation.repository.UserRepository;
import ro.ase.ism.dissertation.specification.StudentSpecifications;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public List<UserDTO> getNonAdminUsers() {
        log.info("Get all non admin users");
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() != Role.ADMIN)
                .map(UserDTO::from)
                .toList();
    }

    public UserDTO getUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        log.info("Retrieving user with id {}", userId);

        return UserDTO.from(user);
    }

    public List<StudentResponse> getFilteredStudents(EducationLevel educationLevel, Integer cohortId, boolean unassignedOnly) {
        Specification<Student> spec = Specification.where(null);

        if (unassignedOnly) {
            spec = spec.and(StudentSpecifications.hasNoStudentGroup());
        }
        if (cohortId != null) {
            spec = spec.and(StudentSpecifications.hasCohortId(cohortId));
        }
        if (educationLevel != null) {
            spec = spec.and(StudentSpecifications.hasEducationLevel(educationLevel));
        }

        return studentRepository.findAll(spec).stream()
                .map(student -> StudentResponse.builder()
                        .id(student.getId())
                        .firstName(student.getFirstName())
                        .lastName(student.getLastName())
                        .email(student.getEmail())
                        .build())
                .toList();
    }
}
