package ro.ase.ism.dissertation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.auth.dto.UserDTO;
import ro.ase.ism.dissertation.model.user.Role;
import ro.ase.ism.dissertation.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<UserDTO> getNonAdminUsers() {
        log.info("Get all non admin users");
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() != Role.ADMIN)
                .map(UserDTO::from)
                .toList();
    }
}
