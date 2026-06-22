package com.vikpix.api.payment.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.vikpix.api.donation.entities.Donation;
import com.vikpix.api.payment.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false, unique = true)
    private Long id;

    @UuidGenerator
    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false, updatable = false, unique = true)
    private String providerPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = true)
    private String providerStatus;

    @Column(nullable = false)
    private Integer amountCents;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String qrCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String qrCodeBase64;

    @Column(nullable = true)
    private String ticketUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime paidAt;

    @Column(nullable = true)
    private LocalDateTime lastWebhookAt;
}