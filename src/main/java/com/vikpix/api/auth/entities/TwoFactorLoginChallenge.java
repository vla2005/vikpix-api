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
@Table(name = "two_factor_login_challenges")
public class TwoFactorLoginChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String token;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String accessTokenEncrypted;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String refreshTokenEncrypted;
    @Column(nullable = true)
    private Long accessTokenExpiresIn;
    @Column(nullable = true)
    private Long refreshTokenExpiresIn;
    @Column(nullable = false)
    private boolean rememberMe;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = true)
    private LocalDateTime usedAt;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    public TwoFactorLoginChallenge(
        String token,
        User user,
        String accessTokenEncrypted,
        String refreshTokenEncrypted,
        Long accessTokenExpiresIn,
        Long refreshTokenExpiresIn,
        boolean rememberMe,
        LocalDateTime expiresAt
    ) {
        this.token = token;
        this.user = user;
        this.accessTokenEncrypted = accessTokenEncrypted;
        this.refreshTokenEncrypted = refreshTokenEncrypted;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        this.rememberMe = rememberMe;
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