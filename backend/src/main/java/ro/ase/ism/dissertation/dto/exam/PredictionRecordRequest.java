package ro.ase.ism.dissertation.dto.exam;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.model.exam.FeatureVector;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PredictionRecordRequest {
    @NotNull
    private Integer examSubmissionId;

    @NotNull
    private Integer windowIndex;

    @NotNull
    private FeatureVector featureVector;

    @NotNull
    private Integer predictedClass;

    @NotNull
    private List<@DecimalMin("0.0") @DecimalMax("1.0") Double> probabilities;
}
