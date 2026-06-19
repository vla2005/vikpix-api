package com.vikpix.api.donation.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vikpix.api.donation.entities.DonationConfigs;

@Repository
public interface DonationConfigRepository extends JpaRepository<DonationConfigs, Long> {
    Optional<DonationConfigs> findByUser_UserName(String userName);

    Optional<DonationConfigs> findByUser_Id(Long userId);
}
