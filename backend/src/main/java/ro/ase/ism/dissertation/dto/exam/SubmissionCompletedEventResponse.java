package ro.ase.ism.dissertation.dto.exam;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionCompletedEventResponse {
    private Integer examSubmissionId;
    private Integer studentId;
    private List<AnswerResponse> answers;
    private Double score;
    private Double grade;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private Instant submittedAt;
}
