package org.solcation.solcation_be.domain.wallet.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.wallet.account.dto.DepositCycleDTO;
import org.solcation.solcation_be.domain.wallet.account.dto.SharedAccountReqDTO;
import org.solcation.solcation_be.domain.wallet.account.dto.SharedAccountResDTO;
import org.solcation.solcation_be.entity.SharedAccount;
import org.solcation.solcation_be.entity.enums.DEPOSITCYCLE;
import org.solcation.solcation_be.entity.enums.DEPOSITDAY;
import org.solcation.solcation_be.repository.GroupRepository;
import org.solcation.solcation_be.repository.SharedAccountRepository;
import org.solcation.solcation_be.util.s3.S3Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharedAccountService {
    private final SharedAccountRepository sharedAccountRepository;
    private final GroupRepository groupRepository;
    private final S3Utils s3Utils;

    @Value("${cloud.s3.bucket.upload.signature}")
    private String UPLOAD_PATH;

    private static final SecureRandom random = new SecureRandom();

    public SharedAccountResDTO getSharedAccountInfo(Long groupId) {
        SharedAccount res = sharedAccountRepository.findByGroup_GroupPk(groupId);
        if (res == null) {
            throw new CustomException(ErrorCode.NOT_EXIST);
        }
        SharedAccountResDTO dto = SharedAccountResDTO.builder()
                .saPk(res.getSaPk())
                .groupPk(groupId)
                .balance(res.getBalance())
                .depositAlarm(res.getDepositAlarm())
                .depositCycle(res.getDepositCycle() != null ? DEPOSITCYCLE.valueOf(res.getDepositCycle().name()) : null)
                .depositDate(res.getDepositDate())
                .depositDay(res.getDepositDay() != null ? DEPOSITDAY.valueOf(res.getDepositDay().name()) : null)
                .depositAmount(res.getDepositAmount())
                .accountNum(res.getAccountNum())
                .saPw(res.getSaPw())
                .build();
        return dto;
    }

    @Transactional
    public Long createSharedAccount(Long groupId, SharedAccountReqDTO dto) {
        var group = groupRepository.findByGroupPk(groupId);
        if (group == null) throw new CustomException(ErrorCode.NOT_EXIST);
        if(sharedAccountRepository.findByGroup_GroupPk(groupId)!=null) throw new CustomException(ErrorCode.ALREADY_EXIST);
        MultipartFile signature = dto.getSignature();
        //확장자 확인(png, jpeg, jpg)
        String originalFilename = signature.getOriginalFilename();

        if(!s3Utils.checkExtension(Objects.requireNonNull(originalFilename))){
            throw new CustomException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        //이미지 업로드
        String filename = s3Utils.uploadObject(signature, originalFilename, UPLOAD_PATH);

        //DB 실패 시 이미지 삭제
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if(status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    try { s3Utils.deleteObject(filename, UPLOAD_PATH); } catch (Exception ignore) {}
                }
                TransactionSynchronization.super.afterCompletion(status);
            }
        });

        group.setSignatureUrl(filename);

        SharedAccount account = SharedAccount.builder()
                .group(group)
                .accountNum(generateAccountNumber(groupId))
                .balance(0)
                .createdAt(Instant.now(Clock.systemUTC()))
                .depositAlarm(false)
                .saPw(dto.getSaPw())
                .build();

        sharedAccountRepository.save(account);

        //TODO: 약관동의 DB 저장

        return account.getSaPk();
    }

    @Transactional
    public void updateDepositCycle(Long groupId, DepositCycleDTO dto) {
        SharedAccount res = sharedAccountRepository.findByGroup_GroupPk(groupId);
        if (res == null) throw new CustomException(ErrorCode.NOT_EXIST);


        res.setDepositAlarm(dto.getDepositAlarm());
        res.setDepositCycle(dto.getDepositCycle());
        if (dto.getDepositCycle() == DEPOSITCYCLE.MONTH) {
            res.setDepositDate(dto.getDepositDate()); // Integer
            res.setDepositDay(null);
        } else if (dto.getDepositCycle() == DEPOSITCYCLE.WEEK) {
            res.setDepositDay(dto.getDepositDay());
            res.setDepositDate(null);                 // Integer라서 가능
        }
        res.setDepositAmount(dto.getDepositAmount());
    }

    private String generateAccountNumber(Long groupId) {
        String bankCode = "110"; // 신한은행 코드
        String branchCode = String.format("%04d", random.nextInt(999) + 1);
        String serialNumber = String.format("%05d", groupId); // PK 기반

       return bankCode + branchCode + serialNumber;
    }
}
