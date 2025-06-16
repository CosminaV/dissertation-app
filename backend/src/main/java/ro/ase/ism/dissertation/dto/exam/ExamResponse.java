package ro.ase.ism.dissertation.dto.exam;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamResponse {
    private Integer id;
    private Boolean isAccessible;
    private String courseName;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private Instant createdAt;
    private Integer durationMinutes;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private Instant examDate;
    private List<ExamQuestionResponse> questions;
    private Boolean submitted;
    private String title;
}
