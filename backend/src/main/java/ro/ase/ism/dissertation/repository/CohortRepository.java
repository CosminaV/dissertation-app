package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.ism.dissertation.model.cohort.Cohort;

import java.util.Optional;

public interface CohortRepository extends JpaRepository<Cohort, Integer> {
    Optional<Cohort> findByName(String name);
}
