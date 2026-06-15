package com.vikpix.api.auth.entities;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import com.vikpix.api.users.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor
@Entity
@Table(name = "two_factor_setups")
public class TwoFactorSetup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String secretEncrypted;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = true)
    private LocalDateTime usedAt;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    public TwoFactorSetup(User user, String secretEncrypted, LocalDateTime expiresAt) {
        this.user = user;
        this.secretEncrypted = secretEncrypted;
        this.expiresAt = expiresAt;
    }
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
    public boolean isUsed() {
        return usedAt != null;
    }
    public void markUsed() {
        this.usedAt = LocalDateTime.now();
    }
}