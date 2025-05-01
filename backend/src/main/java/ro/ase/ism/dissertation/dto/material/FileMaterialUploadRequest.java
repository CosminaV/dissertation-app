package ro.ase.ism.dissertation.dto.material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.model.material.WatermarkType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileMaterialUploadRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private Integer courseGroupId;
    private Integer courseCohortId;

    @NotNull(message = "Watermark type is required")
    private WatermarkType watermarkType;
}
