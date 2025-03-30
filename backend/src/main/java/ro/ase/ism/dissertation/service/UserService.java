package ro.ase.ism.dissertation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.ase.ism.dissertation.auth.dto.UserDTO;
import ro.ase.ism.dissertation.model.user.Role;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                .map(user -> new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole().name()))
                .toList();
    }

    public Map<String, List<UserDTO>> getUsersGroupedByActivationStatus() {
        List<User> users = userRepository.findAll();

        Map<String, List<UserDTO>> groupedUsers = new HashMap<>();
        groupedUsers.put("active", users.stream()
                .filter(User::isActivated)
                .map(UserDTO::from)
                .toList());

        groupedUsers.put("inactive", users.stream()
                .filter(u -> !u.isActivated())
                .map(UserDTO::from)
                .toList());

        return groupedUsers;
    }
}
