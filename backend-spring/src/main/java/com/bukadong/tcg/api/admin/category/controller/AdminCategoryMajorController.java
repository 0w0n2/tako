package com.bukadong.tcg.api.admin.category.controller;

import com.bukadong.tcg.api.admin.category.dto.request.CategoryMajorCreateRequest;
import com.bukadong.tcg.api.admin.category.dto.request.CategoryMajorUpdateRequest;
import com.bukadong.tcg.api.admin.category.dto.response.CategoryMajorResponse;
import com.bukadong.tcg.api.admin.category.service.AdminCategoryCommandService;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.util.MediaDirResolver;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * 카테고리 대분류 관리자 컨트롤러
 * <P>
 * 대분류 생성/수정/삭제를 제공합니다. ROLE_ADMIN만 접근 가능합니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse
 */
@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/v1/admin/categories/majors")
@RequiredArgsConstructor
@Validated
public class AdminCategoryMajorController {

    private final AdminCategoryCommandService adminCategoryCommandService;
    private final MemberQueryService memberQueryService;
    private final MediaDirResolver mediaDirResolver;

    /**
     * 대분류 생성 (멀티파트, 단일 이미지 옵션)
     * <P>
     * body: CategoryMajorCreateRequest(JSON), image: MultipartFile(optional)
     * </P>
     * 
     * @PARAM body 생성 DTO, image 이미지(선택)
     * @RETURN BaseResponse<CategoryMajorResponse>
     */
    @Operation(summary = "대분류 생성", description = "멀티파트로 단일 이미지를 함께 업로드할 수 있습니다. (ADMIN)", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "object", requiredProperties = {
            "requestDto", "image" }), schemaProperties = {
                    @SchemaProperty(name = "requestDto", schema = @Schema(implementation = CategoryMajorCreateRequest.class)),
                    @SchemaProperty(name = "image", schema = @Schema(type = "string", format = "binary")) })))
    @PostMapping(consumes = { MULTIPART_FORM_DATA_VALUE })
    public BaseResponse<CategoryMajorResponse> createWithImage(
            @Parameter(description = "대분류 메타데이터(JSON)", required = true) @Valid @RequestPart("requestDto") CategoryMajorCreateRequest requestDto,
            @Parameter(description = "대표 이미지(선택)") @RequestPart(name = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails user) {

        Member me = memberQueryService.getByUuid(user.getUuid());
        String imageDir = mediaDirResolver.resolve(MediaType.CATEGORY_MAJOR);
        return BaseResponse
                .onSuccess(adminCategoryCommandService.createMajorWithImage(requestDto, image, me, imageDir));
    }

    /**
     * 대분류 수정 (멀티파트, 이미지 대체)
     * <P>
     * body: CategoryMajorUpdateRequest(JSON), image: MultipartFile(optional)
     * </P>
     * 
     * @PARAM majorId ID, body 수정 DTO, image 새 이미지(선택)
     * @RETURN BaseResponse<CategoryMajorResponse>
     */
    @Operation(summary = "대분류 수정", description = "이미지가 첨부되면 기존 이미지를 모두 삭제하고 새 이미지 1장으로 대체합니다. 이미지가 없으면 변경 없습니다. (ADMIN)", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "object", requiredProperties = {
            "requestDto", "image" }), schemaProperties = {
                    @SchemaProperty(name = "requestDto", schema = @Schema(implementation = CategoryMajorUpdateRequest.class)),
                    @SchemaProperty(name = "image", schema = @Schema(type = "string", format = "binary")) })))
    @PutMapping(path = "/{majorId}", consumes = { MULTIPART_FORM_DATA_VALUE })
    public BaseResponse<CategoryMajorResponse> updateWithImage(
            @Parameter(name = "majorId", description = "대분류 ID", required = true) @PathVariable("majorId") @NotNull Long majorId,
            @Parameter(description = "대분류 메타데이터(JSON)", required = true) @Valid @RequestPart("requestDto") CategoryMajorUpdateRequest requestDto,
            @Parameter(description = "새 대표 이미지(선택)") @RequestPart(name = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails user) {

        Member me = memberQueryService.getByUuid(user.getUuid());
        String imageDir = mediaDirResolver.resolve(MediaType.CATEGORY_MAJOR);
        return BaseResponse
                .onSuccess(adminCategoryCommandService.updateMajorWithImage(majorId, requestDto, image, me, imageDir));
    }

    /**
     * 대분류 삭제
     * <P>
     * 하위 중분류/연관 데이터 존재 시 정책에 따라 차단 또는 소프트 삭제합니다.
     * </P>
     * 
     * @PARAM majorId 대분류 ID
     * @RETURN BaseResponse<Void>
     */
    @Operation(summary = "대분류 삭제", description = "카테고리 대분류를 삭제합니다. (ADMIN)")
    @DeleteMapping("/{majorId}")
    public BaseResponse<Void> delete(
            @Parameter(name = "majorId", description = "대분류 ID", required = true) @PathVariable("majorId") @NotNull Long majorId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        adminCategoryCommandService.deleteMajor(majorId, me);
        return BaseResponse.onSuccess();
    }
}
