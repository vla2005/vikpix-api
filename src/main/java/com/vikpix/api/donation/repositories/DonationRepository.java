package com.vikpix.api.donation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vikpix.api.donation.entities.Donation;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long>{

}
