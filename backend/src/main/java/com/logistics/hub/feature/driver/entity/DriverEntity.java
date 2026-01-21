package com.logistics.hub.feature.driver.entity;

import com.logistics.hub.feature.driver.enums.DriverStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing Driver
 */
@Entity
@Table(name = "drivers", uniqueConstraints = {
    @UniqueConstraint(name = "uk_driver_license", columnNames = "license_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Driver full name is required")
    @Column(nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "License number is required")
    @Column(nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @NotNull(message = "Driver status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriverStatus status = DriverStatus.AVAILABLE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}

