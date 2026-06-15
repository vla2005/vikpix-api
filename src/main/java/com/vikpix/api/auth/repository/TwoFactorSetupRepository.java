package com.vikpix.api.auth.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.vikpix.api.auth.entities.TwoFactorSetup;
import com.vikpix.api.users.entities.User;
@Repository
public interface TwoFactorSetupRepository extends JpaRepository<TwoFactorSetup, Long> {
    Optional<TwoFactorSetup> findFirstByUserAndUsedAtIsNullOrderByCreatedAtDesc(User user);
    void deleteByUser(User user);
}