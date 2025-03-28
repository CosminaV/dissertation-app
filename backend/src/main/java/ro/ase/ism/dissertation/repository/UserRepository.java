package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.ism.dissertation.model.user.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByFirstName(String firstName);

    Optional<User> findByActivationToken(String token);
}
