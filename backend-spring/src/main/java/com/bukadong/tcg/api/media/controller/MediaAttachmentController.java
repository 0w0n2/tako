package com.bukadong.tcg.api.media.controller;

import com.bukadong.tcg.api.media.dto.response.MediaUploadResponse;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaAttachmentService;
import com.bukadong.tcg.api.media.util.MediaDirResolver;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.util.List;

/**
 * 범용 미디어 첨부 API
 * <P>
 * 어떤 도메인이든 MediaType/ownerId만 주면 첨부 추가/삭제 가능.
 * </P>
 */
@Tag(name = "Media", description = "범용 미디어 첨부 API")
@RestController
@RequestMapping("/v1/media/attachments")
@RequiredArgsConstructor
@Validated
public class MediaAttachmentController {

    private final MediaAttachmentService mediaAttachmentService;
    private final MemberQueryService memberQueryService;
    private final MediaDirResolver mediaDirResolver;

    /**
     * (멀티파트) 파일 업로드 + 첨부 추가
     */
    @Operation(summary = "파일 업로드 후 첨부 추가", description = "멀티파트 파일을 업로드하고 Media(type, ownerId)에 첨부합니다.")
    @PostMapping(path = "/{type}/{ownerId}", consumes = MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<MediaUploadResponse> addByMultipart(
            @Parameter(description = "MediaType", required = true) @PathVariable(name = "type") MediaType type,
            @Parameter(description = "소유 엔터티 ID", required = true) @PathVariable(name = "ownerId") Long ownerId,
            @Parameter(description = "업로드 파일들", required = true) @RequestPart(name = "files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails user) {
        var me = memberQueryService.getByUuid(user.getUuid());
        String dir = mediaDirResolver.resolve(type);
        MediaUploadResponse res = mediaAttachmentService.addByMultipart(type, ownerId, me, files, dir);
        return BaseResponse.onSuccess(res);
    }

    /**
     * (사전 업로드된) key/url 목록으로 첨부 추가
     */
    @Operation(summary = "사전 업로드 키/URL로 첨부 추가", description = "이미 S3에 올라간 key 또는 full URL 목록을 첨부합니다.")
    @PostMapping("/{type}/{ownerId}/keys")
    public BaseResponse<Void> addByKeys(
            @Parameter(description = "MediaType", required = true) @PathVariable(name = "type") MediaType type,
            @Parameter(description = "소유 엔터티 ID", required = true) @PathVariable(name = "ownerId") Long ownerId,
            @Parameter(description = "S3 key 또는 full URL 목록", required = true) @RequestBody List<@NotNull String> keysOrUrls,
            @Parameter(description = "MIME 타입(옵션)") @RequestParam(name = "mimeType", required = false) String mimeType,
            @AuthenticationPrincipal CustomUserDetails user) {
        var me = memberQueryService.getByUuid(user.getUuid());
        mediaAttachmentService.addByKeys(type, ownerId, me, keysOrUrls, mimeType);
        return BaseResponse.onSuccess();
    }

    /**
     * 첨부 삭제
     */
    @Operation(summary = "첨부 삭제", description = "Media(type, ownerId) 소속의 mediaId를 삭제합니다. S3 오브젝트도 함께 삭제 시도합니다.")
    @DeleteMapping("/{type}/{ownerId}/{mediaId}")
    public BaseResponse<Void> remove(
            @Parameter(description = "MediaType", required = true) @PathVariable(name = "type") MediaType type,
            @Parameter(description = "소유 엔터티 ID", required = true) @PathVariable(name = "ownerId") Long ownerId,
            @Parameter(description = "미디어 ID", required = true) @PathVariable(name = "mediaId") Long mediaId,
            @AuthenticationPrincipal CustomUserDetails user) {
        var me = memberQueryService.getByUuid(user.getUuid());
        mediaAttachmentService.remove(type, ownerId, mediaId, me);
        return BaseResponse.onSuccess();
    }

    /**
     * S3 연결 상태 확인
     */
    @Operation(summary = "S3 연결 헬스체크 테스트", description = "S3 버킷 접근이 가능한지 확인합니다.")
    @GetMapping("/health")
    public BaseResponse<Boolean> healthCheck() {
        boolean ok = mediaAttachmentService.healthCheck();
        return BaseResponse.onSuccess(ok);
    }

}
