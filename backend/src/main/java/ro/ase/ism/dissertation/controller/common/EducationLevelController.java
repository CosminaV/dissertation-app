package ro.ase.ism.dissertation.controller.common;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.config.EducationLevelConfig;
import ro.ase.ism.dissertation.model.course.EducationLevel;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class EducationLevelController {

    @GetMapping("/study-years")
    public ResponseEntity<Map<EducationLevel, Set<Integer>>> getStudyYearsByEducationLevel() {
        Map<EducationLevel, Set<Integer>> studyYearsMap = new EnumMap<>(EducationLevel.class);

        for (EducationLevel educationLevel : EducationLevel.values()) {
            int maxYears = EducationLevelConfig.getMaxYearsOfStudy(educationLevel);
            Set<Integer> years = new TreeSet<>();
            for (int i = 1; i <= maxYears; i++) {
                years.add(i);
            }
            studyYearsMap.put(educationLevel, years);
        }

        return ResponseEntity.ok(studyYearsMap);
    }
}
