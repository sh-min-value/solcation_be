package org.solcation.solcation_be.scheduler.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class DepositAlarmDTO {
    private Long saPk;
    private boolean depositAlarm;
    private Integer depositCycle;
    private Integer depositDate;
    private Integer depositDay;
    private Integer depositAmount;

    private boolean disableAlarm; //유효성 실패
    private boolean notifyToday; //오늘 알림 대상

    public void updateDisableAlarm() {
        this.disableAlarm = true;
    }

    public void updateNotifyToday() {
        this.notifyToday = true;
    }
}
