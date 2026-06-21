package com.vikpix.api.payment.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vikpix.api.payment.entities.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByProviderPaymentId(String providerPaymentId);
}
