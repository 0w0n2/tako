package com.bukadong.tcg.api.admin.card.controller;

import com.bukadong.tcg.api.admin.card.dto.response.CreateCardRequestDto;
import com.bukadong.tcg.api.admin.card.service.AdminCardService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Admin")
@RestController
@RequestMapping("/v1/admin/cards")
@RequiredArgsConstructor
public class AdminCardController {

    private final AdminCardService adminCardService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<Void> createCard(
            @Valid @RequestPart(value = "requestDto") CreateCardRequestDto requestDto,
            @RequestPart(value = "cardImage") MultipartFile cardImage
    ) {
        adminCardService.saveCard(requestDto, cardImage);
        return BaseResponse.onSuccess();
    }
}
