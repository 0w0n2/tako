package com.bukadong.tcg.api.admin.card.controller;

import com.bukadong.tcg.api.admin.card.dto.response.CreateCardRequestDto;
import com.bukadong.tcg.api.admin.card.service.AdminCardService;
import com.bukadong.tcg.api.card.entity.Card;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaAttachmentService;
import com.bukadong.tcg.api.media.util.MediaDirResolver;
import com.bukadong.tcg.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Admin - Cards", description = "카드 관리자 API")
@RestController
@RequestMapping("/v1/admin/cards")
@RequiredArgsConstructor
public class AdminCardController {

    private final AdminCardService adminCardService;
    private final MediaAttachmentService mediaAttachmentService;
    private final MediaDirResolver mediaDirResolver;

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<Void> createCard(
            @Valid @RequestPart CreateCardRequestDto requestDto,
            @RequestPart(value = "cardImage") MultipartFile cardImage
    ) {
        Card savedCard = adminCardService.saveCard(requestDto);
        mediaAttachmentService.addByMultipart(MediaType.CARD, savedCard.getId(), null, List.of(cardImage), mediaDirResolver.resolve(MediaType.CARD));
        return BaseResponse.onSuccess();
    }
}
