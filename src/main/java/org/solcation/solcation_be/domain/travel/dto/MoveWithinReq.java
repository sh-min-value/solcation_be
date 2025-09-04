package org.solcation.solcation_be.domain.travel.dto;

public record MoveWithinReq(
        String crdtId,
        String prevCrdtId,
        String nextCrdtId,
        String clientId,
        Long opTs
) {}
