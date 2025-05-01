package ro.ase.ism.dissertation.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ase.ism.dissertation.dto.cohort.CohortRequest;
import ro.ase.ism.dissertation.dto.cohort.CohortResponse;
import ro.ase.ism.dissertation.exception.ConflictException;
import ro.ase.ism.dissertation.model.cohort.Cohort;
import ro.ase.ism.dissertation.repository.CohortRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCohortService {

    private final CohortRepository cohortRepository;

    @Transactional
    public void createCohort(CohortRequest cohortRequest) {
        boolean nameExists = cohortRepository.findByName(cohortRequest.getName()).isPresent();
        if (nameExists) {
            throw new ConflictException("Cohort with name " + cohortRequest.getName() + " already exists");
        }

        log.info("Creating cohort with name {}", cohortRequest.getName());

        Cohort cohort = Cohort.builder()
                .name(cohortRequest.getName())
                .build();

        cohortRepository.save(cohort);
    }

    @Transactional(readOnly = true)
    public List<CohortResponse> getAllCohorts() {
        log.info("Retrieving all cohorts");
        return cohortRepository.findAll().stream()
                .map(cohort -> CohortResponse.builder()
                        .id(cohort.getId())
                        .name(cohort.getName())
                        .build())
                .toList();
    }
}
