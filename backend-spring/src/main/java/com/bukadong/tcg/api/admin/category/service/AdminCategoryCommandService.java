package com.bukadong.tcg.api.admin.category.service;

import com.bukadong.tcg.api.admin.category.dto.request.CategoryMajorCreateRequest;
import com.bukadong.tcg.api.admin.category.dto.request.CategoryMajorUpdateRequest;
import com.bukadong.tcg.api.admin.category.dto.request.CategoryMediumCreateRequest;
import com.bukadong.tcg.api.admin.category.dto.request.CategoryMediumUpdateRequest;
import com.bukadong.tcg.api.admin.category.dto.response.CategoryMajorResponse;
import com.bukadong.tcg.api.admin.category.dto.response.CategoryMediumResponse;
import com.bukadong.tcg.api.admin.category.repository.AdminCategoryMajorRepository;
import com.bukadong.tcg.api.admin.category.repository.AdminCategoryMediumRepository;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.card.repository.CardRepository;
import com.bukadong.tcg.api.category.entity.CategoryMajor;
import com.bukadong.tcg.api.category.entity.CategoryMedium;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaAttachmentService;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.exception.*;
import com.bukadong.tcg.global.common.base.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import static com.bukadong.tcg.global.common.base.BaseResponseStatus.CATEGORY_MAJOR_NAME_DUPLICATED;
import static com.bukadong.tcg.global.common.base.BaseResponseStatus.CATEGORY_MEDIUM_NAME_DUPLICATED;
import static com.bukadong.tcg.global.common.base.BaseResponseStatus.CATEGORY_NOT_FOUND;
import static com.bukadong.tcg.global.common.base.BaseResponseStatus.CATEGORY_PARENT_NOT_FOUND;
import static com.bukadong.tcg.global.common.base.BaseResponseStatus.INVALID_PARAMETER;

import java.util.Objects;

/**
 * 카테고리 관리자 쓰기 서비스
 * <P>
 * 대분류/중분류의 생성/수정/삭제 등 상태 변경 로직을 담당합니다.
 * </P>
 * 
 * @PARAM 요청 DTO
 * @RETURN 결과 DTO 또는 void
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AdminCategoryCommandService {

    private final AdminCategoryMajorRepository majorRepository;
    private final AdminCategoryMediumRepository mediumRepository;
    private final CardRepository cardRepository;
    private final AuctionRepository auctionRepository;
    private final MediaAttachmentService mediaAttachmentService;

    /**
     * 대분류 생성 (+이미지 1장 옵션)
     * <P>
     * 이미지가 존재하면 CATEGORY_MAJOR(type)로 1장 업로드한다.
     * </P>
     * 
     * @PARAM request 생성 요청
     * @PARAM image 단일 이미지(옵션)
     * @PARAM me 작업자
     * @PARAM imageDir 업로드 디렉토리
     * @RETURN CategoryMajorResponse
     */
    public CategoryMajorResponse createMajorWithImage(CategoryMajorCreateRequest request, MultipartFile image,
            Member me, String imageDir) {
        CategoryMajorResponse res = createMajor(request); // 기존 로직 활용
        if (image != null && !image.isEmpty()) {
            mediaAttachmentService.addByMultipart(MediaType.CATEGORY_MAJOR, res.getId(), me, List.of(image), imageDir);
        }
        return res;
    }

    /**
     * 대분류 생성
     * <P>
     * 이름 중복 등 정책 검증 후 저장합니다.
     * </P>
     * 
     * @PARAM request 생성 요청
     * @RETURN CategoryMajorResponse
     */
    public CategoryMajorResponse createMajor(CategoryMajorCreateRequest request) {
        String name = request.getName();
        String desc = request.getDescription();
        if (majorRepository.existsByName(name)) {
            throw new BaseException(CATEGORY_MAJOR_NAME_DUPLICATED);
        }
        CategoryMajor saved = majorRepository.save(CategoryMajor.of(name, desc));
        return CategoryMajorResponse.from(saved);
    }

    /**
     * 대분류 수정 (+이미지 대체 옵션)
     * <P>
     * 이미지가 존재하면 기존 이미지 전량 삭제 후 새 이미지 1장 등록. 없으면 이미지 변경 없음.
     * </P>
     * 
     * @PARAM majorId ID
     * @PARAM request 수정 요청
     * @PARAM image 새 이미지(옵션)
     * @PARAM me 작업자
     * @PARAM imageDir 업로드 디렉토리
     * @RETURN CategoryMajorResponse
     */
    @Transactional
    public CategoryMajorResponse updateMajorWithImage(Long majorId, CategoryMajorUpdateRequest request,
            MultipartFile image, Member me, String imageDir) {
        CategoryMajorResponse res = updateMajor(majorId, request); // 기존 로직 활용
        if (image != null && !image.isEmpty()) {
            // 기존 이미지 전량 삭제 후 새 이미지 1장
            mediaAttachmentService.removeAll(MediaType.CATEGORY_MAJOR, majorId, me);
            mediaAttachmentService.addByMultipart(MediaType.CATEGORY_MAJOR, majorId, me, List.of(image), imageDir);
        }
        return res;
    }

    /**
     * 대분류 수정
     * <P>
     * 엔티티를 조회 후 변경 감지로 업데이트합니다.
     * </P>
     * 
     * @PARAM majorId 대분류 ID
     * @PARAM request 수정 요청
     * @RETURN CategoryMajorResponse
     */
    public CategoryMajorResponse updateMajor(Long majorId, CategoryMajorUpdateRequest request) {
        CategoryMajor major = majorRepository.findById(majorId)
                .orElseThrow(() -> new BaseException(CATEGORY_NOT_FOUND));

        String name = trimToNull(request.getName());
        String desc = trimToNull(request.getDescription());

        // 최소 1개는 변경 필수
        if (name == null && desc == null) {
            throw new BaseException(INVALID_PARAMETER);
        }

        if (name != null && majorRepository.existsByNameAndIdNot(name, majorId)) {
            throw new BaseException(CATEGORY_MAJOR_NAME_DUPLICATED);
        }

        if (name != null)
            major.updateName(name);
        if (desc != null)
            major.updateDescription(desc);
        return CategoryMajorResponse.from(major);
    }

    /**
     * 대분류 삭제
     * <P>
     * 하위 존재 시 정책에 맞게 예외/소프트 삭제 로직을 넣으세요.
     * </P>
     * 
     * @PARAM majorId 대분류 ID
     * @RETURN void
     */
    public void deleteMajor(Long majorId, Member me) {
        CategoryMajor major = majorRepository.findById(majorId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));

        // 하위 중분류 존재 시 삭제 불가(필요시 구현)
        if (mediumRepository.existsByCategoryMajor_Id(majorId)) {
            throw new BaseException(BaseResponseStatus.CATEGORY_MAJOR_HAS_CHILDREN);
        }

        // 카드/경매 참조 여부
        boolean usedByCard = cardRepository.countByCategoryMajorId(majorId) > 0;
        boolean usedByAuction = auctionRepository.countByCategoryMajorId(majorId) > 0;
        if (usedByCard || usedByAuction) {
            throw new BaseException(BaseResponseStatus.CATEGORY_MAJOR_IN_USE);
        }
        // 관련 미디어 일괄 삭제
        mediaAttachmentService.removeAll(MediaType.CATEGORY_MAJOR, majorId, me);
        majorRepository.delete(major);
    }

    /**
     * 중분류 생성 (+이미지 1장 옵션)
     */
    public CategoryMediumResponse createMediumWithImage(CategoryMediumCreateRequest request, MultipartFile image,
            Member me, String imageDir) {
        CategoryMediumResponse res = createMedium(request);
        if (image != null && !image.isEmpty()) {
            mediaAttachmentService.addByMultipart(MediaType.CATEGORY_MEDIUM, res.getId(), me, List.of(image), imageDir);
        }
        return res;
    }

    /**
     * 중분류 생성
     * <P>
     * 상위 대분류 존재 여부를 검증합니다.
     * </P>
     * 
     * @PARAM request 생성 요청
     * @RETURN CategoryMediumResponse
     */
    public CategoryMediumResponse createMedium(CategoryMediumCreateRequest request) {
        CategoryMajor major = majorRepository.findById(request.getMajorId())
                .orElseThrow(() -> new BaseException(CATEGORY_PARENT_NOT_FOUND));

        String name = request.getName();
        String desc = request.getDescription();

        if (mediumRepository.existsByCategoryMajor_IdAndName(request.getMajorId(), name)) {
            throw new BaseException(CATEGORY_MEDIUM_NAME_DUPLICATED);
        }
        CategoryMedium saved = mediumRepository.save(CategoryMedium.of(major, name, desc));
        return CategoryMediumResponse.from(saved);
    }

    /**
     * 중분류 수정 (+이미지 대체 옵션)
     */
    @Transactional
    public CategoryMediumResponse updateMediumWithImage(Long mediumId, CategoryMediumUpdateRequest request,
            MultipartFile image, Member me, String imageDir) {
        CategoryMediumResponse res = updateMedium(mediumId, request);
        if (image != null && !image.isEmpty()) {
            mediaAttachmentService.removeAll(MediaType.CATEGORY_MEDIUM, mediumId, me);
            mediaAttachmentService.addByMultipart(MediaType.CATEGORY_MEDIUM, mediumId, me, List.of(image), imageDir);
        }
        return res;
    }

    /**
     * 중분류 수정
     * <P>
     * 필요 시 상위 대분류 변경도 지원합니다.
     * </P>
     * 
     * @PARAM mediumId 중분류 ID
     * @PARAM request 수정 요청
     * @RETURN CategoryMediumResponse
     */
    public CategoryMediumResponse updateMedium(Long mediumId, CategoryMediumUpdateRequest request) {
        CategoryMedium medium = mediumRepository.findById(mediumId)
                .orElseThrow(() -> new BaseException(CATEGORY_NOT_FOUND));

        Long newMajorId = request.getMajorId();
        String name = trimToNull(request.getName());
        String desc = trimToNull(request.getDescription());

        // 최소 1개는 변경 필수
        if (newMajorId == null && name == null && desc == null) {
            throw new BaseException(INVALID_PARAMETER);
        }

        if (newMajorId != null && !Objects.equals(medium.getCategoryMajor().getId(), newMajorId)) {
            CategoryMajor newMajor = majorRepository.findById(newMajorId)
                    .orElseThrow(() -> new BaseException(CATEGORY_PARENT_NOT_FOUND));
            medium.changeMajor(newMajor);
        }

        Long targetMajorId = (newMajorId != null) ? newMajorId : medium.getCategoryMajor().getId();
        if (name != null && mediumRepository.existsByCategoryMajor_IdAndNameAndIdNot(targetMajorId, name, mediumId)) {
            throw new BaseException(CATEGORY_MEDIUM_NAME_DUPLICATED);
        }

        if (name != null)
            medium.updateName(name);
        if (desc != null)
            medium.updateDescription(desc);
        return CategoryMediumResponse.from(medium);
    }

    /**
     * 중분류 삭제
     * <P>
     * 연관 데이터 정책을 반영해 삭제합니다.
     * </P>
     * 
     * @PARAM mediumId 중분류 ID
     * @RETURN void
     */
    public void deleteMedium(Long mediumId, Member me) {
        CategoryMedium medium = mediumRepository.findById(mediumId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));
        // 카드/경매 참조 여부
        boolean usedByCard = cardRepository.countByCategoryMediumId(mediumId) > 0;
        boolean usedByAuction = auctionRepository.countByCategoryMediumId(mediumId) > 0;
        if (usedByCard || usedByAuction) {
            throw new BaseException(BaseResponseStatus.CATEGORY_MEDIUM_IN_USE);
        }
        // 관련 미디어 일괄 삭제
        mediaAttachmentService.removeAll(MediaType.CATEGORY_MEDIUM, mediumId, me);
        mediumRepository.delete(medium);
    }

    // 로컬 헬퍼
    private static String trimToNull(String s) {
        return (StringUtils.hasText(s)) ? s : null; // null/공백 → null, 유효하면 trim
    }
}
