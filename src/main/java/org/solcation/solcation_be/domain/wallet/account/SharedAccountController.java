package org.solcation.solcation_be.domain.wallet.account;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.wallet.account.dto.DepositCycleDTO;
import org.solcation.solcation_be.domain.wallet.account.dto.SharedAccountReqDTO;
import org.solcation.solcation_be.domain.wallet.account.dto.SharedAccountResDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "모임통장 계좌 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/group/{groupId:\\d+}/account")
public class SharedAccountController {
    private final SharedAccountService sharedAccountService;

    @GetMapping("/")
    public SharedAccountResDTO getSharedAccount(@PathVariable long groupId) {
        return sharedAccountService.getSharedAccountInfo(groupId);
    }

    @PostMapping(value="/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SharedAccountResDTO createSharedAccount(@PathVariable long groupId,
                                                   @Valid @ModelAttribute SharedAccountReqDTO dto) {
        Long saPk = sharedAccountService.createSharedAccount(groupId, dto);
        return sharedAccountService.getSharedAccountInfo(groupId);
    }

    @PostMapping("/cycle")
    public void updateDepositCycle(@PathVariable long groupId, @RequestBody DepositCycleDTO dto) {
        sharedAccountService.updateDepositCycle(groupId, dto);
    }





}
