package com.logistics.hub.feature.auth.entity;

import com.logistics.hub.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetTokenEntity extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String token;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;
}
