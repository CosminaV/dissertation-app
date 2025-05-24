package ro.ase.ism.dissertation.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private String accessToken;

    @Builder.Default
    private boolean needsPasswordSetup = false;

    @Builder.Default
    private boolean faceImageRequired = false;
}
