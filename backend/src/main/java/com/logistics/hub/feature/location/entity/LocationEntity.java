package com.logistics.hub.feature.location.entity;

import com.logistics.hub.feature.location.enums.LocationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity đại diện cho Location (Điểm địa lý chuẩn hóa)
 * Mục đích: Tránh lặp lat-long, normalize địa chỉ
 */
@Entity
@Table(name = "locations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_location_name", columnNames = "name")
    },
    indexes = {
        @Index(name = "idx_location_type", columnList = "type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên địa điểm không được để trống")
    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Min(value = -90, message = "Latitude phải từ -90 đến 90")
    @Max(value = 90, message = "Latitude phải từ -90 đến 90")
    @NotNull(message = "Latitude không được để trống")
    @Column(nullable = false)
    private Double latitude;

    @Min(value = -180, message = "Longitude phải từ -180 đến 180")
    @Max(value = 180, message = "Longitude phải từ -180 đến 180")
    @NotNull(message = "Longitude không được để trống")
    @Column(nullable = false)
    private Double longitude;

    @NotNull(message = "Loại địa điểm không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LocationType type;

    @Column(length = 500)
    private String address;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
