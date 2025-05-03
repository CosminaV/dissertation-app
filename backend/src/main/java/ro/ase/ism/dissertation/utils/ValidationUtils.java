package ro.ase.ism.dissertation.utils;

import org.springframework.security.access.AccessDeniedException;
import ro.ase.ism.dissertation.config.EducationLevelConfig;
import ro.ase.ism.dissertation.exception.StudyYearNotValidException;
import ro.ase.ism.dissertation.model.course.EducationLevel;

import java.time.LocalDate;
import java.time.Year;

public class ValidationUtils {
    private ValidationUtils() {
    }

    public static void validateYearsOfStudy(int year, EducationLevel educationLevel) {
        int max = EducationLevelConfig.getMaxYearsOfStudy(educationLevel);
        if (year < 1 || year > max) {
            throw new StudyYearNotValidException("Invalid year of study for " + educationLevel +
                    ": must be between 1 and " + max + ".");
        }
    }

    public static void validateAcademicYearUpdates(int year, String message) {
        var currentAcademicYear = getCurrentAcademicYear();

        if (currentAcademicYear != year) {
            throw new AccessDeniedException(message);
        }
    }

    public static int getCurrentAcademicYear() {
        var now = LocalDate.now();
        return (now.getMonthValue() >= 9) ? now.getYear() : now.getYear() -1;
    }
}
