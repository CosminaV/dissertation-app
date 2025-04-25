package ro.ase.ism.dissertation.config;

import ro.ase.ism.dissertation.model.course.EducationLevel;

public class EducationLevelConfig {

    private EducationLevelConfig() {
    }

    public static int getMaxYearsOfStudy(EducationLevel educationLevel) {
        return switch (educationLevel) {
            case BACHELOR, PHD -> 3;
            case MASTER -> 2;
        };
    }
}
