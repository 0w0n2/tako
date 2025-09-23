package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.dto.request.AuctionCreateRequest;
import com.bukadong.tcg.api.auction.dto.response.AuctionCreateResponse;
import com.bukadong.tcg.api.auction.service.AuctionCommandService;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.util.MediaDirResolver;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import java.util.List;

/**
 * 경매 생성 커맨드 컨트롤러
 * <P>
 * 경매를 생성한 뒤 생성된 ID로 이미지 첨부를 수행한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse<AuctionCreateResponse>
 */
@RestController
@RequestMapping("/v1/auctions")
@Tag(name = "Auctions", description = "경매 생성/수정 API")
@RequiredArgsConstructor
public class AuctionCommandController {

    private final AuctionCommandService auctionCommandService;
    private final MemberQueryService memberQueryService;
    private final MediaDirResolver mediaDirResolver;

    @Operation(summary = "경매 생성", description = "requestDto(JSON)과 files(이미지들)는 모두 필수입니다.", requestBody = @RequestBody(required = true, content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "object", requiredProperties = {
            "requestDto", "files" }), schemaProperties = {
                    @SchemaProperty(name = "requestDto", schema = @Schema(implementation = AuctionCreateRequest.class, description = "경매 메타데이터(JSON)")),
                    @SchemaProperty(name = "files", array = @ArraySchema(minItems = 1, schema = @Schema(type = "string", format = "binary"))) })))
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<AuctionCreateResponse> createAuction(
            @Parameter(description = "경매 메타데이터 JSON", required = true) @Valid @RequestPart("requestDto") AuctionCreateRequest requestDto,
            @Parameter(description = "경매 이미지 파일들(1장 이상 필수)", required = true, content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE, array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))) @RequestPart(name = "files", required = true) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        auctionCommandService.isWalletLinked(me);
        String dir = mediaDirResolver.resolve(MediaType.AUCTION_ITEM);
        AuctionCreateResponse result = auctionCommandService.create(requestDto, me, files, dir);
        return BaseResponse.onSuccess(result);
    }
}