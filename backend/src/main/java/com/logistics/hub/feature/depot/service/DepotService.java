package com.logistics.hub.feature.depot.service;

import com.logistics.hub.feature.depot.dto.request.DepotRequest;
import com.logistics.hub.feature.depot.dto.response.DepotResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepotService {

  Page<DepotResponse> findAll(Pageable pageable);

  DepotResponse findById(Long id);

  DepotResponse create(DepotRequest request);

  DepotResponse update(Long id, DepotRequest request);

  void delete(Long id);
}
