package com.logistics.hub.feature.routing.entity;

import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.routing.enums.DistanceSource;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing distance cache between locations
 * Used for OR-Tools optimization
 */
@Entity
@Table(name = "distance_matrix", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"from_location_id", "to_location_id"}))
public class DistanceMatrixEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id", nullable = false)
    private LocationEntity fromLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id", nullable = false)
    private LocationEntity toLocation;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private DistanceSource source = DistanceSource.CALCULATED;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    // ==================== Lifecycle Callbacks ====================

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }

    // ==================== Constructors ====================

    public DistanceMatrixEntity() {
    }

    public DistanceMatrixEntity(LocationEntity fromLocation, LocationEntity toLocation, 
                                 Double distanceKm, Integer durationMinutes) {
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
    }

    // ==================== Getters & Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocationEntity getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(LocationEntity fromLocation) {
        this.fromLocation = fromLocation;
    }

    public LocationEntity getToLocation() {
        return toLocation;
    }

    public void setToLocation(LocationEntity toLocation) {
        this.toLocation = toLocation;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public DistanceSource getSource() {
        return source;
    }

    public void setSource(DistanceSource source) {
        this.source = source;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }
}
