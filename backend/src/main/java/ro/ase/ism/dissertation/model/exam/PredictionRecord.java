package ro.ase.ism.dissertation.model.exam;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prediction_record")
public class PredictionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_submission_id", nullable = false)
    private ExamSubmission examSubmission;

    @Column(nullable = false)
    private Integer windowIndex;

    @Embedded
    private FeatureVector featureVector;

    @Column(nullable = false)
    private Integer predictedClass;

    @ElementCollection
    @CollectionTable(name = "prediction_probabilities", joinColumns = @JoinColumn(name = "prediction_record_id"))
    @Column(name = "probability")
    private List<Double> probabilities = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant timestamp;
}
