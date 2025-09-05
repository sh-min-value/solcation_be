package org.solcation.solcation_be.domain.notification.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record NotificationPageRes(
        List<PushNotificationDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
    public static NotificationPageRes from(Page<PushNotificationDTO> p) {
        return new NotificationPageRes(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.isFirst(),
                p.isLast(),
                p.hasNext(),
                p.hasPrevious()
        );
    }
}
