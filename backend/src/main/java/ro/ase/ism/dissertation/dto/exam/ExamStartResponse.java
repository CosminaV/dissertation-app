package ro.ase.ism.dissertation.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamStartResponse {
    private Integer submissionId;
    private Instant startTime;
}
