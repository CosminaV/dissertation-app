package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.ism.dissertation.model.otp.OneTimePassword;
import ro.ase.ism.dissertation.model.user.User;

import java.util.Optional;

public interface OneTimePasswordRepository extends JpaRepository<OneTimePassword, Long> {
    Optional<OneTimePassword> findByUserAndUsedFalse(User user);
}
