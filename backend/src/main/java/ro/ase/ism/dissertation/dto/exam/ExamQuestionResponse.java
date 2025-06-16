package ro.ase.ism.dissertation.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamQuestionResponse {
    private Integer id;
    private String questionText;
    private List<String> options;
    private Double points;
}
