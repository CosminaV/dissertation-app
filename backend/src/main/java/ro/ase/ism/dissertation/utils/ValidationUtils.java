package ro.ase.ism.dissertation.utils;

import ro.ase.ism.dissertation.config.EducationLevelConfig;
import ro.ase.ism.dissertation.exception.StudyYearNotValidException;
import ro.ase.ism.dissertation.model.course.EducationLevel;

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
}
