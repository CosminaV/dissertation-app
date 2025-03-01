package ro.ase.ism.dissertation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ro.ase.ism.dissertation.model.token.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    @Query("""
        Select rt from RefreshToken rt inner join User u 
        on rt.user.id = u.id
        where rt.user.id = :userId and rt.isLoggedOut = false
    """)
    List<RefreshToken> findAllRefreshTokenByUser(Integer userId);

    Optional<RefreshToken> findByToken(String token);
}
