package ro.ase.ism.dissertation.service.digitalwatermarking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.model.course.CourseGroup;
import ro.ase.ism.dissertation.model.coursecohort.CourseCohort;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.utils.FormatUtils;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatermarkingService {

    private final WatermarkingEncryptionService watermarkingEncryptionService;
    private final ImageService imageService;
    private final StegoService stegoService;

    public BufferedImage generateWatermarkImage(User teacher, CourseGroup courseGroup) {
        String payload = buildInvisibleWatermarkPayload(teacher, courseGroup);
        String encryptedPayload = watermarkingEncryptionService.encrypt(payload);
        BufferedImage baseImage = imageService.generateTransparentImage();

        log.info("Embedding payload into image");
        return stegoService.embed(baseImage, encryptedPayload);
    }

    public BufferedImage generateWatermarkImage(User teacher, CourseCohort courseCohort) {
        String payload = buildInvisibleWatermarkPayload(teacher, courseCohort);
        String encryptedPayload = watermarkingEncryptionService.encrypt(payload);
        BufferedImage baseImage = imageService.generateTransparentImage();

        log.info("Embedding payload into image");
        return stegoService.embed(baseImage, encryptedPayload);
    }

    /*
    Method used for testing purposes
    */
    public String extractWatermarkFromImage(BufferedImage image) {
        String encryptedMessage = stegoService.extract(image);
        String decryptedMessage = watermarkingEncryptionService.decrypt(encryptedMessage);

        log.info("Extracting watermark from image");
        return decryptedMessage;
    }

    public String buildVisibleWatermarkPayload(User teacher) {
        String uploadTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yy HH:mm"));
        return "Uploaded at " + uploadTime + " by " + FormatUtils.formatFullName(teacher.getFirstName(), teacher.getLastName());
    }

    private String buildInvisibleWatermarkPayload(User teacher, CourseGroup courseGroup) {
        String uploadTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yy HH:mm"));
        return String.join("|",
                "TeacherId=" + teacher.getId(),
                "Name=" + FormatUtils.formatFullName(teacher.getFirstName(), teacher.getLastName()),
                "Email=" + teacher.getEmail(),
                "Group=" + courseGroup.getStudentGroup().getName(),
                "Cohort=" + courseGroup.getStudentGroup().getCohort().getName(),
                "Course=" + courseGroup.getCourse().getName(),
                "AcademicYear=" + FormatUtils.formatAcademicYear(courseGroup.getAcademicYear()),
                "UploadedAt=" + uploadTime
                );
    }

    private String buildInvisibleWatermarkPayload(User teacher, CourseCohort courseCohort) {
        String uploadTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yy HH:mm"));
        return String.join("|",
                "TeacherId=" + teacher.getId(),
                "Name=" + FormatUtils.formatFullName(teacher.getFirstName(), teacher.getLastName()),
                "Email=" + teacher.getEmail(),
                "Cohort=" + courseCohort.getCohort().getName(),
                "Course=" + courseCohort.getCourse().getName(),
                "AcademicYear=" + FormatUtils.formatAcademicYear(courseCohort.getAcademicYear()),
                "UploadedAt=" + uploadTime
        );
    }

    /*
    Method used for testing purposes
    */
    public String testExtractWatermark(User teacher, CourseGroup courseGroup) {
        BufferedImage watermarkImage = generateWatermarkImage(teacher, courseGroup);
        String encrypted = stegoService.extract(watermarkImage);
        return watermarkingEncryptionService.decrypt(encrypted);
    }
}
