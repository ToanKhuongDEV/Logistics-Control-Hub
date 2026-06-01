package com.logistics.hub.feature.auth.entity;

import com.logistics.hub.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_username", columnList = "username"),
    @Index(name = "idx_refresh_tokens_jti", columnList = "jti"),
    @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
})
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity extends BaseEntity {

  @Column(nullable = false, length = 36)
  private String jti;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String token;

  @Column(nullable = false, length = 50)
  private String username;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;
}
