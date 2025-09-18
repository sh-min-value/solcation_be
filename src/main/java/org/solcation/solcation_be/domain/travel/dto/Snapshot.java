package org.solcation.solcation_be.domain.travel.dto;

import java.util.List;

public record Snapshot(List<PlanDetailDTO> items, String lastStreamId) {}
