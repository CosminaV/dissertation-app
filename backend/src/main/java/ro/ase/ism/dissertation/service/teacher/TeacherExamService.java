package ro.ase.ism.dissertation.service.teacher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.ism.dissertation.dto.exam.ExamRequest;
import ro.ase.ism.dissertation.dto.exam.ExamResponse;
import ro.ase.ism.dissertation.dto.studentgroup.StudentResponse;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.mapper.ExamMapper;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.exam.Exam;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.repository.CourseCohortRepository;
import ro.ase.ism.dissertation.repository.ExamRepository;
import ro.ase.ism.dissertation.repository.StudentRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherExamService {

    private final CourseCohortRepository courseCohortRepository;
    private final TeacherAccessService teacherAccessService;
    private final ExamRepository examRepository;
    private final ExamMapper examMapper;
    private final StudentRepository studentRepository;

    @Transactional
    public ExamResponse createExam(ExamRequest examRequest, Integer teacherId) {
        CourseCohort courseCohort = courseCohortRepository.findById(examRequest.getCourseCohortId())
                .orElseThrow(() -> new EntityNotFoundException("Course cohort not found"));

        teacherAccessService.validateTeacherAccessToCourseCohort(courseCohort, teacherId);

        log.info("Teacher {} is creating exam", teacherId);

        Exam exam = examMapper.toEntity(examRequest, courseCohort);
        examRepository.save(exam);

        return examMapper.toFullResponse(exam);
    }

    @Transactional(readOnly = true)
    public ExamResponse getExamDetails(Integer examId, Integer teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found"));

        CourseCohort courseCohort = exam.getCourseCohort();
        teacherAccessService.validateTeacherAccessToCourseCohort(courseCohort, teacherId);

        return examMapper.toFullResponse(exam);
    }

    public List<ExamResponse> getExamsForCourseCohort(Integer courseCohortId, Integer teacherId) {
        CourseCohort courseCohort = courseCohortRepository.findById(courseCohortId)
                .orElseThrow(() -> new EntityNotFoundException("Course cohort not found"));

        teacherAccessService.validateTeacherAccessToCourseCohort(courseCohort, teacherId);

        List<Exam> exams = examRepository.findByCourseCohort(courseCohort);

        log.info("Getting exams for course cohort {}", courseCohortId);

        return exams.stream()
                .map(examMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsForExam(Integer examId, Integer teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found"));

        CourseCohort courseCohort = exam.getCourseCohort();
        teacherAccessService.validateTeacherAccessToCourseCohort(courseCohort, teacherId);

        Cohort cohort = courseCohort.getCohort();
        List<Student> students = studentRepository.findByCohortId(cohort.getId());

        return students.stream()
                .map(student -> StudentResponse.builder()
                        .id(student.getId())
                        .firstName(student.getFirstName())
                        .lastName(student.getLastName())
                        .email(student.getEmail())
                        .build())
        .toList();
    }
}
