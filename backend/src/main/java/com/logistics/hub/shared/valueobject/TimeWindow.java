package com.logistics.hub.shared.valueobject;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeWindow {

    @NotNull(message = "Start time is required")
    private Instant startTime;

    @NotNull(message = "End time is required")
    private Instant endTime;

    public boolean isValid() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    public boolean contains(Instant time) {
        return time.isAfter(startTime) && time.isBefore(endTime);
    }

    public boolean overlaps(TimeWindow other) {
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }
}
