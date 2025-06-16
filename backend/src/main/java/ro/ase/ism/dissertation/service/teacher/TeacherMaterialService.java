package ro.ase.ism.dissertation.service.teacher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ro.ase.ism.dissertation.dto.material.FileMaterialUploadRequest;
import ro.ase.ism.dissertation.dto.material.TextMaterialUploadRequest;
import ro.ase.ism.dissertation.dto.material.MaterialDownloadResponse;
import ro.ase.ism.dissertation.dto.material.MaterialResponse;
import ro.ase.ism.dissertation.dto.material.UpdateMaterialRequest;
import ro.ase.ism.dissertation.exception.EntityNotFoundException;
import ro.ase.ism.dissertation.exception.InvalidFileFormatException;
import ro.ase.ism.dissertation.exception.MaterialCorruptedException;
import ro.ase.ism.dissertation.exception.MissingTargetException;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.material.Material;
import ro.ase.ism.dissertation.model.material.WatermarkType;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.CourseCohortRepository;
import ro.ase.ism.dissertation.repository.CourseGroupRepository;
import ro.ase.ism.dissertation.repository.MaterialRepository;
import ro.ase.ism.dissertation.repository.UserRepository;
import ro.ase.ism.dissertation.service.FileStorageService;
import ro.ase.ism.dissertation.mapper.MaterialMapper;
import ro.ase.ism.dissertation.service.digitalwatermarking.PdfWatermarkEmbedder;
import ro.ase.ism.dissertation.service.digitalwatermarking.WatermarkingService;

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
    private final CourseCohortRepository courseCohortRepository;
    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final FileStorageService fileStorageService;
    private final WatermarkingService watermarkingService;
    private final PdfWatermarkEmbedder pdfWatermarkEmbedder;
    private final TeacherAccessService teacherAccessService;
    private final MaterialMapper materialMapper;

    public List<MaterialResponse> getMaterialsForCourseGroup(Integer courseGroupId, Integer teacherId) {
        CourseGroup courseGroup = courseGroupRepository.findById(courseGroupId)
                .orElseThrow(() -> new EntityNotFoundException("Course group not found"));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        teacherAccessService.validateTeacherAccessToCourseGroup(courseGroup, teacherId);

        log.info("Retrieving materials for course group with id {}", courseGroupId);

        List<Material> materials = materialRepository.findByCourseGroupAndTeacher(courseGroup, teacher);

        return materials.stream()
                .map(materialMapper::mapToMaterialResponse)
        .toList();
    }

    public List<MaterialResponse> getMaterialsForCourseCohort(Integer courseCohortId, Integer teacherId) {
        CourseCohort courseCohort = courseCohortRepository.findById(courseCohortId)
                .orElseThrow(() -> new EntityNotFoundException("Course cohort not found"));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        teacherAccessService.validateTeacherAccessToCourseCohort(courseCohort, teacherId);

        log.info("Retrieving materials for course cohort with id {}", courseCohortId);

        List<Material> materials = materialRepository.findByCourseCohortAndTeacher(courseCohort, teacher);

        return materials.stream()
                .map(materialMapper::mapToMaterialResponse)
                .toList();
    }

    @Transactional
    public MaterialResponse createTextMaterial(TextMaterialUploadRequest request, Integer teacherId) {
        if ((request.getCourseGroupId() == null && request.getCourseCohortId() == null) ||
                (request.getCourseGroupId() != null && request.getCourseCohortId() != null)) {
            throw new MissingTargetException("Either courseGroupId or courseCohortId must be provided");
        }

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        Material.MaterialBuilder materialBuilder = Material.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .teacher(teacher);

        if (request.getCourseGroupId() != null) {
            CourseGroup courseGroup = courseGroupRepository.findById(request.getCourseGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("Course group not found"));
            teacherAccessService.validateTeacherAccessToCourseGroup(courseGroup, teacherId);
            materialBuilder.courseGroup(courseGroup);
        } else {
            CourseCohort courseCohort = courseCohortRepository.findById(request.getCourseCohortId())
                    .orElseThrow(() -> new EntityNotFoundException("Course cohort not found"));
            teacherAccessService.validateTeacherAccessToCourseCohort(courseCohort, teacherId);
            materialBuilder.courseCohort(courseCohort);
        }

        Material material = materialBuilder.build();
        materialRepository.save(material);

        return materialMapper.mapToMaterialResponse(material);
    }

    @Transactional
    public MaterialResponse createFileMaterial(FileMaterialUploadRequest request, MultipartFile file, Integer teacherId) {
        if ((request.getCourseGroupId() == null && request.getCourseCohortId() == null) ||
                (request.getCourseGroupId() != null && request.getCourseCohortId() != null)) {
            throw new MissingTargetException("Either courseGroupId or courseCohortId must be provided");
        }

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        MultipartFile fileToUpload = file;

        Material material;
        if (request.getCourseGroupId() != null) {
            CourseGroup courseGroup = courseGroupRepository.findById(request.getCourseGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("Course group not found"));
            teacherAccessService.validateTeacherAccessToCourseGroup(courseGroup, teacherId);
            material = handleFileMaterialUpload(request.getTitle(), fileToUpload, teacher, request.getWatermarkType(), courseGroup, null);
        } else {
            CourseCohort courseCohort = courseCohortRepository.findById(request.getCourseCohortId())
                    .orElseThrow(() -> new EntityNotFoundException("Course cohort not found"));
            teacherAccessService.validateTeacherAccessToCourseCohort(courseCohort, teacherId);
            material = handleFileMaterialUpload(request.getTitle(), fileToUpload, teacher, request.getWatermarkType(), null, courseCohort);
        }

        materialRepository.save(material);
        return materialMapper.mapToMaterialResponse(material);
    }

    public MaterialDownloadResponse downloadMaterial(Integer materialId, Integer teacherId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));

        if (material.getFilePath() == null) {
            throw new MaterialCorruptedException("Material does not contain a file");
        }

        teacherAccessService.validateTeacherOwnsMaterial(material, teacherId);

        log.info("Teacher {} is downloading material with id {}", teacherId, materialId);

        InputStream inputStream = fileStorageService.downloadFile(material.getFilePath());

        return MaterialDownloadResponse.builder()
                .inputStream(inputStream)
                .originalFilename(material.getOriginalFileName())
                .contentType(material.getContentType())
                .build();
    }

    /*
    Method used for testing purposes
     */
    public String extractWatermark(Integer materialId, Integer teacherId) {
        MaterialDownloadResponse downloadResponse = downloadMaterial(materialId, teacherId);

        log.info("Extracting hidden watermarked image from file");
        BufferedImage watermarkImage = pdfWatermarkEmbedder.extractWatermarkImageFromPdf(downloadResponse.getInputStream());

        String decryptedMessage = watermarkingService.extractWatermarkFromImage(watermarkImage);
        return decryptedMessage;
    }

    @Transactional
    public void deleteMaterial(Integer materialId, Integer teacherId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));

        if (material.getFilePath() == null) {
            throw new MaterialCorruptedException("Material does not contain a file");
        }

        teacherAccessService.validateTeacherOwnsMaterial(material, teacherId);

        log.info("Deleting material with id {}", materialId);

        if (material.getFilePath() != null) {
            fileStorageService.deleteFile(material.getFilePath());
        }

        materialRepository.delete(material);
    }

    @Transactional
    public MaterialResponse updateMaterial(Integer id, UpdateMaterialRequest request, Integer teacherId) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));

        teacherAccessService.validateTeacherOwnsMaterial(material, teacherId);

        log.info("Updating material with id {}", id);

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

    private Material handleFileMaterialUpload(
            String title,
            MultipartFile file,
            User teacher,
            WatermarkType watermarkType,
            CourseGroup courseGroup,
            CourseCohort courseCohort
    ) {
        MultipartFile fileToUpload = file;

        if (isRealPdf(fileToUpload)) {
            if (watermarkType == WatermarkType.INVISIBLE) {
                log.info("Embedding invisible watermark into PDF file");
                if (courseGroup != null) {
                    BufferedImage watermarkedImage = watermarkingService.generateWatermarkImage(teacher, courseGroup);
                    ByteArrayInputStream watermarkedPdfStream = pdfWatermarkEmbedder.embedWatermark(file, watermarkedImage);
                    fileToUpload = getFileToUpload(file, watermarkedPdfStream);
                } else if (courseCohort != null) {
                    BufferedImage watermarkedImage = watermarkingService.generateWatermarkImage(teacher, courseCohort);
                    ByteArrayInputStream watermarkedPdfStream = pdfWatermarkEmbedder.embedWatermark(file, watermarkedImage);
                    fileToUpload = getFileToUpload(file, watermarkedPdfStream);
                } else {
                    throw new MissingTargetException("Both courseGroup and courseCohort are null while embedding invisible watermark");
                }

            } else if (watermarkType == WatermarkType.VISIBLE) {
                String watermarkText = watermarkingService.buildVisibleWatermarkPayload(teacher);
                log.info("Embedding visible watermark into PDF file");
                ByteArrayInputStream watermarkedPdfStream = pdfWatermarkEmbedder.embedVisibleWatermark(file, watermarkText);
                fileToUpload = getFileToUpload(file, watermarkedPdfStream);
            }
        } else {
            throw new InvalidFileFormatException("File does not have valid PDF format");
        }

        String prefix = (courseGroup != null) ?
                "course-group-" + courseGroup.getId() :
                "course-cohort-" + courseCohort.getId();

        String objectKey = fileStorageService.uploadFile(prefix, fileToUpload);

        return Material.builder()
                .title(title)
                .content(null)
                .filePath(objectKey)
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .teacher(teacher)
                .courseGroup(courseGroup)
                .courseCohort(courseCohort)
                .build();
    }

    private MultipartFile getFileToUpload(MultipartFile file, ByteArrayInputStream watermarkedPdfStream) {
        return new InMemoryMultipartFile(
                "file",
                file.getOriginalFilename(),
                file.getContentType(),
                watermarkedPdfStream.readAllBytes()
        );
    }

    private MaterialResponse updateTextMaterial(Material material, UpdateMaterialRequest request) {
        if (request.getTitle() != null) {
            material.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            material.setContent(request.getContent());
        }
        materialRepository.save(material);
        return materialMapper.mapToMaterialResponse(material);
    }

    private MaterialResponse updateFileMaterial(Material material, UpdateMaterialRequest request) {
        if (request.getTitle() != null) {
            material.setTitle(request.getTitle());
        }
        materialRepository.save(material);
        return materialMapper.mapToMaterialResponse(material);
    }

    static class InMemoryMultipartFile extends MockMultipartFile {
        public InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            super(name, originalFilename, contentType, content);
        }
    }
}
