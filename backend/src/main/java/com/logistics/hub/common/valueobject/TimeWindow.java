package com.logistics.hub.common.valueobject;

import com.logistics.hub.common.constant.MessageConstant;
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

    @NotNull(message = MessageConstant.START_TIME_REQUIRED)
    private Instant startTime;

    @NotNull(message = MessageConstant.END_TIME_REQUIRED)
    private Instant endTime;

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isValid() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean contains(Instant time) {
        return time.isAfter(startTime) && time.isBefore(endTime);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean overlaps(TimeWindow other) {
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }
}
