package com.vikpix.api.donation.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.vikpix.api.donation.enums.DonationStatus;
import com.vikpix.api.users.entities.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false, unique = true)
    private Long id;

    @UuidGenerator
    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(updatable = false, nullable = false)
    private String donorIp;

    @Column(updatable = false, nullable = true)
    private String donorName;

    @Column(updatable = false, nullable = false)
    private Integer amountCents;

    @Column(updatable = false, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
