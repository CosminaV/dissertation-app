package ro.ase.ism.dissertation.dto.material;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaterialDownloadResponse {
    private InputStream inputStream;
    private String originalFilename;
    private String contentType;
}
