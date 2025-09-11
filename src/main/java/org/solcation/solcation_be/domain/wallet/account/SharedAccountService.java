package org.solcation.solcation_be.domain.wallet.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.wallet.account.dto.*;
import org.solcation.solcation_be.entity.SharedAccount;
import org.solcation.solcation_be.entity.TermsAgreement;
import org.solcation.solcation_be.entity.TermsCategory;
import org.solcation.solcation_be.entity.enums.DEPOSITCYCLE;
import org.solcation.solcation_be.entity.enums.DEPOSITDAY;
import org.solcation.solcation_be.repository.GroupRepository;
import org.solcation.solcation_be.repository.SharedAccountRepository;
import org.solcation.solcation_be.repository.TermsAgreementRepository;
import org.solcation.solcation_be.repository.TermsCategoryRepository;
import org.solcation.solcation_be.util.s3.S3Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharedAccountService {
    private final SharedAccountRepository sharedAccountRepository;
    private final GroupRepository groupRepository;
    private final TermsCategoryRepository termsCategoryRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final S3Utils s3Utils;

    @Value("${cloud.s3.bucket.upload.signature}")
    private String UPLOAD_PATH;

    private static final SecureRandom random = new SecureRandom();

    public SharedAccountResDTO getSharedAccountInfo(Long groupId) {
        SharedAccount res = sharedAccountRepository.findByGroup_GroupPk(groupId);
        if (res == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_ACCOUNT);
        }
        SharedAccountResDTO dto = SharedAccountResDTO.builder()
                .saPk(res.getSaPk())
                .groupPk(groupId)
                .balance(res.getBalance())
                .depositAlarm(res.getDepositAlarm())
                .depositCycle(res.getDepositCycle() != null ? DEPOSITCYCLE.valueOf(res.getDepositCycle().name()) : null)
                .depositDate(res.getDepositDate() != null ? res.getDepositDate() : null)
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
        if(sharedAccountRepository.findByGroup_GroupPk(groupId)!=null) throw new CustomException(ErrorCode.ACCOUNT_ALREADY_EXISTS);

        var signature = dto.getSignature();
        //확장자 확인(png, jpeg, jpg)
        String originalFilename = signature.getOriginalFilename();

        log.info("signature filename='{}', contentType='{}', size={}",
                signature.getOriginalFilename(), signature.getContentType(), signature.getSize());

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

        group.updateSignatureUrl(filename);
        groupRepository.save(group);

        SharedAccount account = SharedAccount.builder()
                .group(group)
                .accountNum(generateAccountNumber(groupId))
                .balance(0)
                .createdAt(Instant.now())
                .depositAlarm(false)
                .saPw(dto.getSaPw())
                .build();

        sharedAccountRepository.save(account);

        List<TermsCategory> list = termsCategoryRepository.findAll();

        for(TermsCategory tc : list) {
            TermsAgreement terms = TermsAgreement.builder()
                    .termsPk(tc)
                    .isAgree(true)
                    .group(group)
                    .build();
            termsAgreementRepository.save(terms);
        }

        return account.getSaPk();
    }

    @Transactional
    public void updateDepositCycle(Long groupId, DepositCycleDTO dto) {
        SharedAccount res = sharedAccountRepository.findByGroup_GroupPk(groupId);
        if (res == null) throw new CustomException(ErrorCode.NOT_FOUND_ACCOUNT);

        sharedAccountRepository.updateDepositCycle(dto.getSaPk(),
                dto.getDepositAlarm(),
                dto.getDepositCycle(),
                dto.getDepositDate(),
                dto.getDepositDay(),
                dto.getDepositAmount());
    }

    private String generateAccountNumber(Long groupId) {
        String bankCode = "110"; // 신한은행 코드
        String branchCode = String.format("%04d", random.nextInt(999) + 1);
        String serialNumber = String.format("%05d", groupId); // PK 기반

       return bankCode + branchCode + serialNumber;
    }
}
