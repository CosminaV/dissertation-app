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
public class PredictionRecordResponse {
    private Integer id;
    private Integer examSubmissionId;
    private Integer studentId;
    private Integer windowIndex;
    private Integer predictedClass;
    private List<Double> probabilities;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private Instant timestamp;
}
