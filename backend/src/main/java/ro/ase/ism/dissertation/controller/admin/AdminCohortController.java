package ro.ase.ism.dissertation.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.ase.ism.dissertation.dto.cohort.CohortRequest;
import ro.ase.ism.dissertation.dto.cohort.CohortResponse;
import ro.ase.ism.dissertation.service.AdminCohortService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cohorts")
@RequiredArgsConstructor
public class AdminCohortController {

    private final AdminCohortService adminCohortService;

    @PostMapping
    public ResponseEntity<Void> createCohort(@RequestBody CohortRequest cohortRequest) {
        adminCohortService.createCohort(cohortRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<CohortResponse>> getAllCohorts() {
        return ResponseEntity.ok(adminCohortService.getAllCohorts());
    }
}
