package org.solcation.solcation_be.domain.travel.dto;

public record MoveDayReq(
        String crdtId,
        int newDay,
        String prevCrdtId,
        String nextCrdtId,
        String clientId,
        Long opTs
) {}