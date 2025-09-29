package com.bukadong.tcg.api.admin.notice.controller;

import com.bukadong.tcg.api.admin.notice.dto.request.NoticeCreateRequest;
import com.bukadong.tcg.api.admin.notice.dto.request.NoticeUpdateRequest;
import com.bukadong.tcg.api.admin.notice.dto.response.NoticeResponse;
import com.bukadong.tcg.api.admin.notice.service.AdminNoticeCommandService;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.util.MediaDirResolver;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * 공지사항 관리자 컨트롤러
 * <P>
 * 제목/본문과 함께 이미지/첨부파일을 업로드/갱신/삭제합니다. 보안은 /v1/admin/** 시큐리티 설정에 따릅니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse
 */
@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/v1/admin/notices")
@RequiredArgsConstructor
@Validated
public class AdminNoticeController {

    private final AdminNoticeCommandService adminNoticeCommandService;
    private final MemberQueryService memberQueryService;
    private final MediaDirResolver mediaDirResolver;

    /**
     * 공지 생성(첨부 포함)
     * <P>
     * requestDto(JSON) + images[] + attachments[]를 동시에 업로드합니다. 파일은 선택입니다.
     * </P>
     * 
     * @PARAM requestDto NoticeCreateRequest(JSON)
     * @PARAM images 이미지 파일들(옵션)
     * @PARAM attachments 일반 첨부(옵션, zip/pdf 등)
     * @RETURN BaseResponse<NoticeResponse>
     */
    @Operation(summary = "공지 생성(관리자, 첨부 포함)", description = "제목/본문 + 이미지/첨부파일을 함께 업로드합니다.", requestBody = @RequestBody(required = true, content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "object", requiredProperties = {
            "requestDto", "images", "attachments" }), schemaProperties = {
                    @SchemaProperty(name = "requestDto", schema = @Schema(implementation = NoticeCreateRequest.class)),
                    @SchemaProperty(name = "images", array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))),
                    @SchemaProperty(name = "attachments", array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))) })))
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<NoticeResponse> create(
            @Parameter(description = "공지 메타데이터(JSON)", required = true) @Valid @RequestPart("requestDto") NoticeCreateRequest requestDto,
            @Parameter(description = "이미지 파일들(옵션)", content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE)) @RequestPart(name = "images", required = false) List<MultipartFile> images,
            @Parameter(description = "일반 첨부파일들(옵션)", content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE)) @RequestPart(name = "attachments", required = false) List<MultipartFile> attachments,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        String imageDir = mediaDirResolver.resolve(MediaType.NOTICE);
        String attachDir = mediaDirResolver.resolve(MediaType.NOTICE_ATTACHMENT);
        NoticeResponse res = adminNoticeCommandService.create(me, requestDto, images, imageDir, attachments, attachDir);
        return BaseResponse.onSuccess(res);
    }

    /**
     * 공지 수정(첨부 추가/선택적 초기화)
     * <P>
     * 제목/본문만 수정하거나, 파일을 추가로 업로드할 수 있습니다. clearImages/clearAttachments=true면 기존 파일을
     * 전부 지우고 새 파일로 대체합니다.
     * </P>
     * 
     * @PARAM noticeId 공지 ID
     * @PARAM requestDto 수정 DTO
     * @PARAM images 추가/대체 이미지(옵션)
     * @PARAM attachments 추가/대체 첨부(옵션)
     * @PARAM clearImages 기존 이미지 전체 삭제 여부
     * @PARAM clearAttachments 기존 첨부 전체 삭제 여부
     * @RETURN BaseResponse<NoticeResponse>
     */
    @Operation(summary = "공지 수정(관리자, 첨부 포함)", description = "제목/본문을 수정하고 이미지/첨부를 추가 또는 전체 교체합니다.", requestBody = @RequestBody(required = true, content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "object", requiredProperties = {
            "requestDto", "images", "attachments" }), schemaProperties = {
                    @SchemaProperty(name = "requestDto", schema = @Schema(implementation = NoticeUpdateRequest.class)),
                    @SchemaProperty(name = "images", array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))),
                    @SchemaProperty(name = "attachments", array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))) })))
    @PutMapping(value = "/{noticeId}", consumes = MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<NoticeResponse> update(
            @Parameter(description = "공지 ID", required = true) @PathVariable("noticeId") @Min(1) Long noticeId,
            @Parameter(description = "공지 메타데이터(JSON)", required = true) @Valid @RequestPart("requestDto") NoticeUpdateRequest requestDto,
            @Parameter(description = "이미지 파일들(옵션)", content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE)) @RequestPart(name = "images", required = false) List<MultipartFile> images,
            @Parameter(description = "일반 첨부파일들(옵션)", content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE)) @RequestPart(name = "attachments", required = false) List<MultipartFile> attachments,
            @Parameter(description = "기존 이미지 전체 삭제 후 대체 여부") @RequestParam(name = "clearImages", defaultValue = "false") boolean clearImages,
            @Parameter(description = "기존 첨부 전체 삭제 후 대체 여부") @RequestParam(name = "clearAttachments", defaultValue = "false") boolean clearAttachments,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        String imageDir = mediaDirResolver.resolve(MediaType.NOTICE);
        String attachDir = mediaDirResolver.resolve(MediaType.NOTICE_ATTACHMENT);
        NoticeResponse res = adminNoticeCommandService.update(noticeId, requestDto, me, images, imageDir, clearImages,
                attachments, attachDir, clearAttachments);
        return BaseResponse.onSuccess(res);
    }

    /**
     * 공지 삭제(첨부 포함)
     * <P>
     * 공지와 연관된 모든 이미지/첨부를 정리한 뒤 삭제합니다.
     * </P>
     * 
     * @PARAM noticeId 공지 ID
     * @RETURN BaseResponse<Void>
     */
    @Operation(summary = "공지 삭제(관리자)", description = "공지와 그에 연결된 모든 첨부파일을 삭제합니다.")
    @DeleteMapping("/{noticeId}")
    public BaseResponse<Void> delete(
            @Parameter(description = "공지 ID", required = true) @PathVariable("noticeId") @Min(1) Long noticeId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        adminNoticeCommandService.delete(noticeId, me);
        return BaseResponse.onSuccess();
    }
}
