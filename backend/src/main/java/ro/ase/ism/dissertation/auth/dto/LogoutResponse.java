package ro.ase.ism.dissertation.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogoutResponse {

    private String message;

    public LogoutResponse(String message) {
        this.message = message;
    }

}
