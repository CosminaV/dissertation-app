package ro.ase.ism.dissertation.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.ase.ism.dissertation.model.user.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private boolean activated;

    public static UserDTO from(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .activated(user.getPassword() != null)
                .build();
    }
}
