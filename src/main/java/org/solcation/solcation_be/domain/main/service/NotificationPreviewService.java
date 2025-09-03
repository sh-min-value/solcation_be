package org.solcation.solcation_be.domain.main.service;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.main.dto.NotificationPreviewDTO;
import org.solcation.solcation_be.repository.PushNotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationPreviewService {

    private final PushNotificationRepository pushNotificationRepository;

    public List<NotificationPreviewDTO> getNotificationPreview(Long userPk) {
        var top2 = pushNotificationRepository.findTop2ByUserPk_UserPkOrderByPnTimeDesc(userPk);
        return top2.stream()
                .map(n -> NotificationPreviewDTO.builder()
                        .acName(n.getAcPk().getAcName())
                        .groupName(n.getGroupPk().getGroupName())
                        .groupLeader(n.getGroupPk().getGroupLeader().getUserName())
                        .build())
                .toList();
    }
}
