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
@Table(name = "two_factor_recovery_codes")
public class TwoFactorRecoveryCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private String codeHash;
    @Column(nullable = true)
    private LocalDateTime usedAt;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    public TwoFactorRecoveryCode(User user, String codeHash) {
        this.user = user;
        this.codeHash = codeHash;
    }
    public boolean isUsed() {
        return usedAt != null;
    }
    public void markUsed() {
        this.usedAt = LocalDateTime.now();
    }
}