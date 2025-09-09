package org.solcation.solcation_be.domain.travel.dto;

public record UpdateReq(
        String crdtId,
        String pdPlace,
        String pdAddress,
        int pdCost,
        Long tcPk,
        String clientId,
        Long opTs
) {}
