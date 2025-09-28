package com.bukadong.tcg.api.admin.card.controller;

import com.bukadong.tcg.api.admin.card.dto.response.CreateCardRequestDto;
import com.bukadong.tcg.api.admin.card.dto.response.NftCreateResponseDto;
import com.bukadong.tcg.api.admin.card.service.AdminCardService;
import com.bukadong.tcg.api.card.service.PhysicalCardService;
import com.bukadong.tcg.global.common.base.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final PhysicalCardService physicalCardService;

    @Operation(summary = "카드 생성(이미지 포함)", description = "requestDto(JSON) + cardImage를 동시에 업로드합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<Void> createCard(
            @Parameter(description = "카드 메타데이터(JSON)", required = true) @Valid @RequestPart(value = "requestDto") CreateCardRequestDto requestDto,
            @Parameter(description = "카드 이미지 파일", required = true) @RequestPart(value = "cardImage") MultipartFile cardImage) {
        adminCardService.saveCard(requestDto, cardImage);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "특정 카드의 NFT 발행 요청", description = "cardId에 해당하는 카드의 실물 NFT를 발행하고 클레임용 시크릿을 등록합니다. 실제 블록체인 컨트랙트는 비동기로 처리됩니다.")
    @PostMapping("/{cardId}/nfts")
    public BaseResponse<NftCreateResponseDto> requestNftMint(
            @PathVariable("cardId") Long cardId) {
        return BaseResponse.onSuccess(physicalCardService.requestNftCreation(cardId));
    }
}
