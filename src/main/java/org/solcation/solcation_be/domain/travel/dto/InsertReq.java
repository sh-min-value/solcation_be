package org.solcation.solcation_be.domain.travel.dto;

public record InsertReq(
        int pdDay,
        String prevCrdtId,
        String nextCrdtId,
        String pdPlace,
        String pdAddress,
        int pdCost,
        Long tcPk,
        String clientId,
        Long opTs
) {}
