package ro.ase.ism.dissertation.dto.exam;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamSubmissionResponse {
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private Instant startedAt;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private Instant submittedAt;
}
