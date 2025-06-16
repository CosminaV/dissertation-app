package ro.ase.ism.dissertation.dto.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnswerResponse {
    private Integer questionId;
    private Integer selectedIndex;
    private Boolean isCorrect;
}
