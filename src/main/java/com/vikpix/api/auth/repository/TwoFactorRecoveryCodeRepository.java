package com.vikpix.api.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vikpix.api.auth.entities.TwoFactorRecoveryCode;
import com.vikpix.api.users.entities.User;

@Repository
public interface TwoFactorRecoveryCodeRepository extends JpaRepository<TwoFactorRecoveryCode, Long> {
    List<TwoFactorRecoveryCode> findByUserAndUsedAtIsNull(User user);

    void deleteByUser(User user);
}