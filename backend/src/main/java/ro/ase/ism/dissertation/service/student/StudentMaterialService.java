package ro.ase.ism.dissertation.service.student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.dto.material.MaterialDownloadResponse;
import ro.ase.ism.dissertation.dto.material.MaterialResponse;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.exception.MaterialCorruptedException;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.material.Material;
import ro.ase.ism.dissertation.model.user.Student;
import ro.ase.ism.dissertation.repository.CourseCohortRepository;
import ro.ase.ism.dissertation.repository.CourseGroupRepository;
import ro.ase.ism.dissertation.repository.MaterialRepository;
import ro.ase.ism.dissertation.repository.StudentRepository;
import ro.ase.ism.dissertation.service.FileStorageService;
import ro.ase.ism.dissertation.mapper.MaterialMapper;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentMaterialService {

    private final CourseGroupRepository courseGroupRepository;
    private final CourseCohortRepository courseCohortRepository;
    private final StudentRepository studentRepository;
    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;
    private final StudentAccessService studentAccessService;
    private final FileStorageService fileStorageService;

    public List<MaterialResponse> getMaterialsForCourseGroup(Integer studentId, Integer courseGroupId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> new EntityNotFoundException("Course group not found"));

        // Check if the student is enrolled in the same student group and academic year
        studentAccessService.validateStudentAccessToCourseGroup(student, courseGroup);

        List<Material> materials = materialRepository.findByCourseGroup(courseGroup);
        return materials.stream().map(materialMapper::mapToMaterialResponse).toList();
    }

    public List<MaterialResponse> getMaterialsForCourseCohort(Integer studentId, Integer courseCohortId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        CourseCohort courseCohort = courseCohortRepository.findById(courseCohortId)
                .orElseThrow(() -> new EntityNotFoundException("Course cohort not found"));

        // Check if the student is enrolled in the same cohort and academic year
        studentAccessService.validateStudentAccessToCourseCohort(student, courseCohort);

        List<Material> materials = materialRepository.findByCourseCohort(courseCohort);
        return materials.stream().map(materialMapper::mapToMaterialResponse).toList();
    }

    public MaterialDownloadResponse downloadMaterial(Integer materialId, Integer studentId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        if (material.getFilePath() == null) {
            throw new MaterialCorruptedException("Material does not contain a file");
        }

        studentAccessService.validateStudentAccessToMaterial(student, material);

        log.info("Student {} is downloading material {}", studentId, materialId);

        InputStream inputStream = fileStorageService.downloadFile(material.getFilePath());

        return MaterialDownloadResponse.builder()
                .inputStream(inputStream)
                .originalFilename(material.getOriginalFileName())
                .contentType(material.getContentType())
                .build();
    }
}
