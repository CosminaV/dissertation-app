package ro.ase.ism.dissertation.model.exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureVector {

    @JsonProperty("head_entropy")
    private Double headEntropy;

    @JsonProperty("eye_entropy")
    private Double eyeEntropy;

    @JsonProperty("head_direction_changes")
    private Double headDirectionChanges;

    @JsonProperty("eye_direction_changes")
    private Double eyeDirectionChanges;

    @JsonProperty("face_verified_ratio")
    private Double faceVerifiedRatio;

    @JsonProperty("multiple_faces_ratio")
    private Double multipleFacesRatio;

    @JsonProperty("no_face_ratio")
    private Double noFaceRatio;

    @JsonProperty("phone_detected_ratio")
    private Double phoneDetectedRatio;

    @JsonProperty("mean_liveness_score")
    private Double meanLivenessScore;
}
