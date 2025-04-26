package ro.ase.ism.dissertation.service.digitalwatermarking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.model.course.Course;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.course.StudentGroup;
import ro.ase.ism.dissertation.model.user.User;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestWatermarkService {

    private final WatermarkingService watermarkingService;
    private final PdfWatermarkEmbedder pdfWatermarkEmbedder;

    @Transactional(readOnly = true)
    public String testWatermark() {
        User teacher = new User();
        teacher.setId(1);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setEmail("john.doe@example.com");

        StudentGroup group = new StudentGroup();
        group.setName("1010");
        Cohort cohort = new Cohort();
        cohort.setName("CohortA");
        group.setCohort(cohort);

        Course course = new Course();
        course.setName("Advanced Java");

        CourseGroup courseGroup = new CourseGroup();
        courseGroup.setCourse(course);
        courseGroup.setStudentGroup(group);
        courseGroup.setAcademicYear(2024);

        return watermarkingService.testExtractWatermark(teacher, courseGroup);
    }

    public String extractWatermarkFromUploadedFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage watermarkImage = pdfWatermarkEmbedder.extractWatermarkImageFromPdf(inputStream);

            String decryptedMessage = watermarkingService.extractWatermarkFromImage(watermarkImage);
            return decryptedMessage;

        } catch (IOException e) {
            throw new RuntimeException("Failed to extract watermark", e);
        }
    }
}
