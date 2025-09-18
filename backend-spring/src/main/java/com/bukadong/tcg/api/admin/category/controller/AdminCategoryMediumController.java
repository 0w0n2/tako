package com.bukadong.tcg.api.admin.category.controller;

import com.bukadong.tcg.api.admin.category.dto.request.CategoryMediumCreateRequest;
import com.bukadong.tcg.api.admin.category.dto.request.CategoryMediumUpdateRequest;
import com.bukadong.tcg.api.admin.category.dto.response.CategoryMediumResponse;
import com.bukadong.tcg.api.admin.category.service.AdminCategoryCommandService;
import com.bukadong.tcg.api.admin.notice.dto.request.NoticeUpdateRequest;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.util.MediaDirResolver;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * 카테고리 중분류 관리자 컨트롤러
 * <P>
 * 중분류 생성/수정/삭제를 제공합니다. ROLE_ADMIN만 접근 가능합니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse
 */
@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/v1/admin/categories/mediums")
@RequiredArgsConstructor
@Validated
public class AdminCategoryMediumController {

    private final AdminCategoryCommandService adminCategoryCommandService;
    private final MemberQueryService memberQueryService;
    private final MediaDirResolver mediaDirResolver;

    /**
     * 중분류 생성
     * <P>
     * 멀티파트로 단일 이미지를 함께 업로드할 수 있습니다. 이미지가 없으면 데이터만 생성됩니다.
     * </P>
     * 
     * @PARAM body 생성 DTO(JSON), image 이미지(선택), user 인증 사용자
     * @RETURN BaseResponse<CategoryMediumResponse>
     */
    @Operation(summary = "중분류 생성", description = "멀티파트로 단일 이미지를 함께 업로드할 수 있습니다. (ADMIN)", requestBody = @RequestBody(required = true, content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "object", requiredProperties = {
            "requestDto", "image" }), schemaProperties = {
                    @SchemaProperty(name = "requestDto", schema = @Schema(implementation = CategoryMediumCreateRequest.class)),
                    @SchemaProperty(name = "image", schema = @Schema(type = "string", format = "binary")) })))
    @PostMapping(consumes = { MULTIPART_FORM_DATA_VALUE })
    public BaseResponse<CategoryMediumResponse> createWithImage(
            @Parameter(description = "카테고리 메타데이터(JSON)", required = true) @Valid @RequestPart("requestDto") CategoryMediumCreateRequest requestDto,
            @Parameter(description = "대표 이미지(선택)", content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE)) @RequestPart(name = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails user) {

        Member me = memberQueryService.getByUuid(user.getUuid());
        String imageDir = mediaDirResolver.resolve(MediaType.CATEGORY_MEDIUM);
        return BaseResponse
                .onSuccess(adminCategoryCommandService.createMediumWithImage(requestDto, image, me, imageDir));
    }

    /**
     * 중분류 수정(이미지 대체)
     * <P>
     * 이미지가 첨부되면 기존 이미지를 전부 삭제하고 새 이미지 1장으로 대체합니다. 이미지가 없으면 변경 없습니다.
     * </P>
     * 
     * @PARAM mediumId 중분류 ID
     * @PARAM body 수정 DTO(JSON)
     * @PARAM image 새 이미지(선택)
     * @PARAM user 인증 사용자
     * @RETURN BaseResponse<CategoryMediumResponse>
     */
    @Operation(summary = "중분류 수정", description = "이미지가 첨부되면 기존 이미지를 모두 삭제하고 새 이미지 1장으로 대체합니다. 이미지가 없으면 변경 없습니다. (ADMIN)", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "object", requiredProperties = {
            "requestDto", "image" }), schemaProperties = {
                    @SchemaProperty(name = "requestDto", schema = @Schema(implementation = CategoryMediumUpdateRequest.class)),
                    @SchemaProperty(name = "image", schema = @Schema(type = "string", format = "binary")) })))
    @PutMapping(path = "/{mediumId}", consumes = { MULTIPART_FORM_DATA_VALUE })
    public BaseResponse<CategoryMediumResponse> updateWithImage(
            @Parameter(name = "mediumId", description = "중분류 ID", required = true) @PathVariable("mediumId") @NotNull Long mediumId,
            @Parameter(description = "중분류 메타데이터(JSON)", required = true) @Valid @RequestPart("requestDto") CategoryMediumUpdateRequest requestDto,
            @Parameter(description = "새 대표 이미지(선택)") @RequestPart(name = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails user) {

        Member me = memberQueryService.getByUuid(user.getUuid());
        String imageDir = mediaDirResolver.resolve(MediaType.CATEGORY_MEDIUM);
        return BaseResponse.onSuccess(
                adminCategoryCommandService.updateMediumWithImage(mediumId, requestDto, image, me, imageDir));
    }

    /**
     * 중분류 삭제
     * <P>
     * 연관 데이터가 있으면 정책에 따라 차단 또는 소프트 삭제합니다.
     * </P>
     * 
     * @PARAM mediumId 중분류 ID
     * @RETURN BaseResponse<Void>
     */
    @Operation(summary = "중분류 삭제", description = "카테고리 중분류를 삭제합니다. (ADMIN)")
    @DeleteMapping("/{mediumId}")
    public BaseResponse<Void> delete(
            @Parameter(name = "mediumId", description = "중분류 ID", required = true) @PathVariable("mediumId") @NotNull Long mediumId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        adminCategoryCommandService.deleteMedium(mediumId, me);
        return BaseResponse.onSuccess();
    }
}
