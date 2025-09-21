package com.bukadong.tcg.api.admin.card.controller;

import com.bukadong.tcg.api.admin.card.service.AdminPhysicalCardService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/physical-cards")
public class AdminPhysicalCardController {

    private final AdminPhysicalCardService adminPhysicalCardService;

    @GetMapping("/{physicalCardId}/status")
    public BaseResponse<?> getNftStatus(@PathVariable("physicalCardId") Long physicalCardId) {
        return BaseResponse.onSuccess(adminPhysicalCardService.getPhysicalCardStatus(physicalCardId));
    }
}
