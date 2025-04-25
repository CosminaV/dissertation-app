package ro.ase.ism.dissertation.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.dto.coursecohort.AssignCourseCohortRequest;
import ro.ase.ism.dissertation.dto.coursecohort.CourseCohortResponse;
import ro.ase.ism.dissertation.service.AdminCourseCohortService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/course-cohorts")
@RequiredArgsConstructor
public class AdminCourseCohortController {

    private final AdminCourseCohortService adminCourseCohortService;

    @PostMapping
    public ResponseEntity<Void> assignLectureTeacherToCohort(
            @RequestBody AssignCourseCohortRequest request) {
        adminCourseCohortService.assignLectureTeacherToCohort(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<CourseCohortResponse>> getAllCourseCohorts() {
        List<CourseCohortResponse> responses = adminCourseCohortService.getAllCourseCohorts();
        return ResponseEntity.ok(responses);
    }
}
