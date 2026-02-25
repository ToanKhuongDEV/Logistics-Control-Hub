package com.logistics.hub.feature.routing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatrixResult implements Serializable {
    private long[][] distanceMatrix;
    private int[][] durationMatrix;
}
