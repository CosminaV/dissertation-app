package ro.ase.ism.dissertation.dto.material;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaterialResponse {
    private Integer id;
    private String title;
    private String content;
    private String filePath;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime uploadDate;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime lastUpdatedAt;
    private String courseName;
    private String studentGroupName;
    private String uploadedBy;
}
