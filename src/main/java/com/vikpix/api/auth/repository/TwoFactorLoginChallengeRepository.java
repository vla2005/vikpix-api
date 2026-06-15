package com.vikpix.api.auth.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.vikpix.api.auth.entities.TwoFactorLoginChallenge;
@Repository
public interface TwoFactorLoginChallengeRepository extends JpaRepository<TwoFactorLoginChallenge, Long> {
    Optional<TwoFactorLoginChallenge> findByToken(String token);
}