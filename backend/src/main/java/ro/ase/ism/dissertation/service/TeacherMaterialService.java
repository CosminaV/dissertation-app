package ro.ase.ism.dissertation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ro.ase.ism.dissertation.dto.material.CreateMaterialRequest;
import ro.ase.ism.dissertation.dto.material.MaterialDownloadResponse;
import ro.ase.ism.dissertation.dto.material.MaterialResponse;
import ro.ase.ism.dissertation.dto.material.UpdateMaterialRequest;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.exception.InvalidFileFormatException;
import ro.ase.ism.dissertation.exception.WatermarkingFailedException;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.course.Material;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.CourseGroupRepository;
import ro.ase.ism.dissertation.repository.MaterialRepository;
import ro.ase.ism.dissertation.repository.UserRepository;
import ro.ase.ism.dissertation.service.digitalwatermarking.PdfWatermarkEmbedder;
import ro.ase.ism.dissertation.service.digitalwatermarking.WatermarkingService;
import ro.ase.ism.dissertation.utils.FormatUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherMaterialService {

    private final CourseGroupRepository courseGroupRepository;
    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final FileStorageService fileStorageService;
    private final WatermarkingService watermarkingService;
    private final PdfWatermarkEmbedder pdfWatermarkEmbedder;

    public List<MaterialResponse> getMaterialsForCourseGroup(Integer courseGroupId) {
        CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> new EntityNotFoundException("Course group not found"));

        log.info("Retrieving materials for course group with id {}", courseGroupId);

        List<Material> materials = materialRepository.findByCourseGroup(courseGroup);

        return materials.stream()
                .map(this::mapToMaterialResponse)
        .toList();
    }

    @Transactional
    public MaterialResponse createTextMaterial(CreateMaterialRequest request, Integer teacherId) {
        CourseGroup courseGroup = courseGroupRepository.findById(request.getCourseGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Course group not found"));

        User teacher = userRepository.findById(teacherId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        validateTeacherAccessToCourseGroup(courseGroup, teacherId);

        log.info("Teacher with id {} is uploading a text material", teacherId);

        Material material = Material.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .filePath(null)
                .courseGroup(courseGroup)
                .teacher(teacher)
                .build();
        materialRepository.save(material);

        return mapToMaterialResponse(material);
    }

    @Transactional
    public MaterialResponse createFileMateriel(String title, MultipartFile file, Integer courseGroupId, Integer teacherId) {
        CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> new EntityNotFoundException("Course group not found"));

        User teacher = userRepository.findById(teacherId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        validateTeacherAccessToCourseGroup(courseGroup, teacherId);

        log.info("Teacher with id {} is uploading a file material", teacherId);

        String prefix = "course-group-" + courseGroupId;

        MultipartFile fileToUpload = file;

        if (isRealPdf(fileToUpload)) {
            try {
                BufferedImage watermarkedImage = watermarkingService.generateWatermarkImage(teacher, courseGroup);

                log.info("Embedding watermark into file");
                ByteArrayInputStream watermarkedPdfStream = pdfWatermarkEmbedder.embedWatermark(file.getInputStream(), watermarkedImage);

                fileToUpload = new InMemoryMultipartFile(
                        "file",
                        file.getOriginalFilename(),
                        file.getContentType(),
                        watermarkedPdfStream.readAllBytes()
                );
            } catch (IOException e) {
                throw new WatermarkingFailedException("Failed to generate watermarked PDF");
            }
        } else {
            throw new InvalidFileFormatException("File does not have valid format");
        }

        String objectKey =  fileStorageService.uploadFile(prefix, fileToUpload);

        Material material = Material.builder()
                .title(title)
                .content(null)
                .filePath(objectKey)
                .courseGroup(courseGroup)
                .teacher(teacher)
                .build();
        materialRepository.save(material);

        return mapToMaterialResponse(material);
    }

    public MaterialDownloadResponse downloadMaterial(Integer materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));

        if (material.getFilePath() == null) {
            throw new IllegalStateException("Material does not contain a file");
        }

        validateTeacherAccessToCourseGroup(material.getCourseGroup(), material.getTeacher().getId());

        log.info("Downloading material with id {}", materialId);

        InputStream inputStream = fileStorageService.downloadFile(material.getFilePath());
        String contentType = fileStorageService.getContentType(material.getFilePath());

        return MaterialDownloadResponse.builder()
                .inputStream(inputStream)
                .originalFilename(fileStorageService.extractOriginalFileName(material.getFilePath()))
                .contentType(contentType)
                .build();
    }

    /*
    Method used for testing purposes
     */
    public String extractWatermark(Integer materialId) {
        MaterialDownloadResponse downloadResponse = downloadMaterial(materialId);

        log.info("Extracting hidden watermarked image from file");
        BufferedImage watermarkImage = pdfWatermarkEmbedder.extractWatermarkImageFromPdf(downloadResponse.getInputStream());

        String decryptedMessage = watermarkingService.extractWatermarkFromImage(watermarkImage);
        return decryptedMessage;
    }

    @Transactional
    public void deleteMaterial(Integer materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));

        log.info("Deleting material with id {}", materialId);

        validateTeacherAccessToCourseGroup(material.getCourseGroup(), material.getTeacher().getId());

        if (material.getFilePath() != null) {
            fileStorageService.deleteFile(material.getFilePath());
        }

        materialRepository.delete(material);
    }

    @Transactional
    public MaterialResponse updateMaterial(Integer id, UpdateMaterialRequest request, Integer teacherId) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));

        if (!material.getTeacher().getId().equals(teacherId)) {
            throw new AccessDeniedException("You are not allowed to edit this material");
        }

        validateTeacherAccessToCourseGroup(material.getCourseGroup(), teacherId);

        if (material.getFilePath() == null) {
            return updateTextMaterial(material, request);
        } else {
            return updateFileMaterial(material, request);
        }
    }

    private boolean isRealPdf(MultipartFile file) {
        if (file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
            return false;
        }

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            if (is.read(header) != 4) {
                return false;
            }
            String headerStr = new String(header);
            return headerStr.equals("%PDF");
        } catch (IOException e) {
            return false;
        }
    }

    private MaterialResponse updateTextMaterial(Material material, UpdateMaterialRequest request) {
        if (request.getTitle() != null) {
            material.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            material.setContent(request.getContent());
        }
        materialRepository.save(material);
        return mapToMaterialResponse(material);
    }

    private MaterialResponse updateFileMaterial(Material material, UpdateMaterialRequest request) {
        if (request.getTitle() != null) {
            material.setTitle(request.getTitle());
        }
        materialRepository.save(material);
        return mapToMaterialResponse(material);
    }

    private MaterialResponse mapToMaterialResponse(Material material) {
        return MaterialResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .content(material.getContent())
                .filePath(material.getFilePath())
                .uploadDate(material.getUploadDate())
                .lastUpdatedAt(material.getLastUpdatedAt())
                .courseName(material.getCourseGroup().getCourse().getName())
                .studentGroupName(material.getCourseGroup().getStudentGroup().getName())
                .uploadedBy(FormatUtils.formatFullName(material.getTeacher().getFirstName(), material.getTeacher().getLastName()))
                .build();
    }

    private void validateTeacherAccessToCourseGroup(CourseGroup courseGroup, Integer teacherId) {
        Course course = courseGroup.getCourse();
        Cohort cohort = courseGroup.getStudentGroup().getCohort();

        boolean isPracticalTeacher = courseGroup.getPracticalTeacher().getId().equals(teacherId);

        boolean isLectureTeacher = course.getCourseCohorts().stream()
                .anyMatch(cc -> cc.getCohort().equals(cohort)
                        && cc.getLectureTeacher().getId().equals(teacherId));

        if (!isPracticalTeacher && !isLectureTeacher) {
            throw new AccessDeniedException("You are not allowed to upload materials for this course group");
        }
    }

    static class InMemoryMultipartFile extends MockMultipartFile {
        public InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            super(name, originalFilename, contentType, content);
        }
    }
}
