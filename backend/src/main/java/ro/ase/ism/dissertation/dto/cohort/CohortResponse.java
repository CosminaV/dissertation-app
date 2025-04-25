package ro.ase.ism.dissertation.dto.cohort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CohortResponse {
    private Integer id;
    private String name;
}
