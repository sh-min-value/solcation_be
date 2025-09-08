package org.solcation.solcation_be.entity.enums;

import lombok.Getter;

@Getter
public enum ALARMCODE {
    GROUP_INVITE("그룹 초대 알림", "그룹에서 초대를 보냈어요!"),
    DEPOSIT_REMINDER("정기 납부일 알림", "정기 납부일 입니다! 모임통장으로 이동해 확인해주세요."),
    TRAVEL_CREATED("여행 추가 알림", "새로운 여행이 추가되었어요! 그룹으로 이동해 확인해주세요."),
    ACCOUNT_CREATED("모임통장 개설 알림", "모임통장이 개설되었어요!");

    private final String title;
    private final String content;

    private ALARMCODE(String title,String content) {
        this.title = title;
        this.content = content;
    }
}
