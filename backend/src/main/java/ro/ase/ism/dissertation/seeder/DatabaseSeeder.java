package ro.ase.ism.dissertation.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.course.EducationLevel;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.user.Role;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.CohortRepository;
import ro.ase.ism.dissertation.repository.CourseCohortRepository;
import ro.ase.ism.dissertation.repository.CourseGroupRepository;
import ro.ase.ism.dissertation.repository.CourseRepository;
import ro.ase.ism.dissertation.repository.StudentGroupRepository;
import ro.ase.ism.dissertation.repository.StudentRepository;
import ro.ase.ism.dissertation.repository.UserRepository;

import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final CohortRepository cohortRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseCohortRepository courseCohortRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String password = passwordEncoder.encode("GoodPassword1234!");

        // 1. Cohorts
        Cohort cohortA = cohortRepository.save(new Cohort(null, "A", null, null));
        Cohort cohortB = cohortRepository.save(new Cohort(null, "B", null, null));
        Cohort cohortC = cohortRepository.save(new Cohort(null, "C", null, null));

        // 2. Student Groups
        StudentGroup g1010 = studentGroupRepository.save(new StudentGroup(null, "1010", 1, EducationLevel.BACHELOR, null, null, cohortA));
        StudentGroup g1011 = studentGroupRepository.save(new StudentGroup(null, "1011", 1, EducationLevel.BACHELOR, null, null, cohortA));
        StudentGroup g1020 = studentGroupRepository.save(new StudentGroup(null, "1020", 2, EducationLevel.BACHELOR, null, null, cohortA));
        StudentGroup g1030 = studentGroupRepository.save(new StudentGroup(null, "1030", 3, EducationLevel.BACHELOR, null, null, cohortA));
        StudentGroup g1100 = studentGroupRepository.save(new StudentGroup(null, "1100", 1, EducationLevel.MASTER, null, null, cohortB));
        StudentGroup g1200 = studentGroupRepository.save(new StudentGroup(null, "1200", 1, EducationLevel.PHD, null, null, cohortC));

        // 3. Users (Students + Teachers)
        User teacher1 = User.builder()
                .email("teacher1@gradus.com")
                .firstName("Ana")
                .lastName("Voicu")
                .password(password)
                .role(Role.TEACHER)
                .build();

        User teacher2 = User.builder()
                .email("teacher2@gradus.com")
                .firstName("Radu")
                .lastName("Ilie")
                .password(password)
                .role(Role.TEACHER)
                .build();

        User teacher3 = User.builder()
                .email("teacher3@gradus.com")
                .firstName("Ioana")
                .lastName("Cristea")
                .password(password)
                .role(Role.TEACHER)
                .build();

        User teacher4 = User.builder()
                .email("teacher4@gradus.com")
                .firstName("Andrei")
                .lastName("Zamfir")
                .password(password)
                .role(Role.TEACHER)
                .build();

        User teacher5 = User.builder()
                .email("teacher5@gradus.com")
                .firstName("Mara")
                .lastName("Serban")
                .password(password)
                .role(Role.TEACHER)
                .build();

        User teacher6 = User.builder()
                .email("teacher6@gradus.com")
                .firstName("Tudor")
                .lastName("Neagu")
                .password(password)
                .role(Role.TEACHER)
                .build();

        User teacher7 = User.builder()
                .email("teacher7@gradus.com")
                .firstName("Bianca")
                .lastName("Stoica")
                .password(password)
                .role(Role.TEACHER)
                .build();

        User teacher8 = User.builder()
                .email("teacher8@gradus.com")
                .firstName("Matei")
                .lastName("Iacob")
                .password(password)
                .role(Role.TEACHER)
                .build();

        List<User> teachers = List.of(teacher1, teacher2, teacher3, teacher4, teacher5, teacher6, teacher7, teacher8);
        userRepository.saveAll(teachers);

        Student s1 = Student.builder()
                .email("alice@student.com")
                .firstName("Alice")
                .lastName("Pop")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1010)
                .build();

        Student s2 = Student.builder()
                .email("bob@student.com")
                .firstName("Delia")
                .lastName("Ionescu")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1010)
                .build();

        Student s3 = Student.builder()
                .email("carla@student.com")
                .firstName("Carla")
                .lastName("Moraru")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1010)
                .build();

        Student s4 = Student.builder()
                .email("dan@student.com")
                .firstName("Dan")
                .lastName("Marin")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1010)
                .build();

        Student s5 = Student.builder()
                .email("ella@student.com")
                .firstName("Ella")
                .lastName("Stan")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1011)
                .build();

        Student s6 = Student.builder()
                .email("felix@student.com")
                .firstName("Felix")
                .lastName("Dobrescu")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1011)
                .build();

        Student s7 = Student.builder()
                .email("gina@student.com")
                .firstName("Gina")
                .lastName("Toma")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1020)
                .build();

        Student s8 = Student.builder()
                .email("hugo@student.com")
                .firstName("Hugo")
                .lastName("Roman")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1020)
                .build();

        Student s9 = Student.builder()
                .email("irina@student.com")
                .firstName("Irina")
                .lastName("Petrescu")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1030)
                .build();

        Student s10 = Student.builder()
                .email("jason@student.com")
                .firstName("Jason")
                .lastName("Luca")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1030)
                .build();

        Student s11 = Student.builder()
                .email("katia@student.com")
                .firstName("Katia")
                .lastName("Vlad")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1030)
                .build();

        Student s12 = Student.builder()
                .email("leo@student.com")
                .firstName("Leo")
                .lastName("Andrei")
                .password(password)
                .role(Role.STUDENT)
                .educationLevel(EducationLevel.BACHELOR)
                .studentGroup(g1030)
                .build();

        studentRepository.saveAll(List.of(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12));
        // 4. Courses
        List<Course> courses = List.of(
                new Course(null, "Intro to Java", 1, 1, EducationLevel.BACHELOR, null, null, null),
                new Course(null, "Data Structures", 1, 2, EducationLevel.BACHELOR, null, null, null),
                new Course(null, "Databases", 2, 1, EducationLevel.BACHELOR, null, null, null),
                new Course(null, "Web Dev", 2, 2, EducationLevel.BACHELOR, null, null, null),
                new Course(null, "OS", 3, 1, EducationLevel.BACHELOR, null, null, null),
                new Course(null, "Security", 3, 2, EducationLevel.BACHELOR, null, null, null),
                new Course(null, "Advanced ML", 1, 1, EducationLevel.MASTER, null, null, null),
                new Course(null, "Deep Learning", 1, 2, EducationLevel.MASTER, null, null, null),
                new Course(null, "Research Methods", 1, 1, EducationLevel.PHD, null, null, null),
                new Course(null, "Scientific Writing", 1, 2, EducationLevel.PHD, null, null, null)
        ).stream().map(courseRepository::save).toList();

        // 5. Course Cohorts
        courseCohortRepository.save(new CourseCohort(null, 2024, courses.get(0), cohortA, teachers.get(0), null));
        courseCohortRepository.save(new CourseCohort(null, 2024, courses.get(1), cohortA, teachers.get(1), null));
        courseCohortRepository.save(new CourseCohort(null, 2024, courses.get(2), cohortB, teachers.get(2), null));
        courseCohortRepository.save(new CourseCohort(null, 2024, courses.get(3), cohortB, teachers.get(3), null));
        courseCohortRepository.save(new CourseCohort(null, 2024, courses.get(4), cohortB, teachers.get(4), null));
        courseCohortRepository.save(new CourseCohort(null, 2024, courses.get(5), cohortB, teachers.get(5), null));
        courseCohortRepository.save(new CourseCohort(null, 2024, courses.get(6), cohortC, teachers.get(6), null));
        courseCohortRepository.save(new CourseCohort(null, 2024, courses.get(7), cohortC, teachers.get(7), null));

        // 6. Course Groups
        courseGroupRepository.save(new CourseGroup(null, 2024, courses.get(0), g1010, teachers.get(1), null, null));
        courseGroupRepository.save(new CourseGroup(null, 2024, courses.get(1), g1010, teachers.get(3), null, null));
        courseGroupRepository.save(new CourseGroup(null, 2024, courses.get(2), g1020, teachers.get(4), null, null));
        courseGroupRepository.save(new CourseGroup(null, 2024, courses.get(3), g1020, teachers.get(5), null, null));
        courseGroupRepository.save(new CourseGroup(null, 2024, courses.get(4), g1030, teachers.get(6), null, null));
        courseGroupRepository.save(new CourseGroup(null, 2024, courses.get(5), g1030, teachers.get(7), null, null));
        courseGroupRepository.save(new CourseGroup(null, 2024, courses.get(6), g1100, teachers.get(0), null, null));
        courseGroupRepository.save(new CourseGroup(null, 2024, courses.get(7), g1100, teachers.get(1), null, null));
        courseGroupRepository.save(new CourseGroup(null, 2024, courses.get(8), g1200, teachers.get(2), null, null));
        courseGroupRepository.save(new CourseGroup(null, 2024, courses.get(9), g1200, teachers.get(3), null, null));

        log.info("Initial test data loaded successfully.");
    }
}
