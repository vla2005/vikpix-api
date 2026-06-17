package com.vikpix.api.donation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vikpix.api.donation.entities.DonationConfigs;

@Repository
public interface DonationConfigRepository extends JpaRepository<DonationConfigs, Long> {

}
