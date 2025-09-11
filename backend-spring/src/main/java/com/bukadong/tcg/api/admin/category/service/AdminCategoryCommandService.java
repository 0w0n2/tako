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
import com.bukadong.tcg.global.common.exception.*;
import com.bukadong.tcg.global.common.base.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@RequiredArgsConstructor
public class AdminCategoryCommandService {

    private final AdminCategoryMajorRepository majorRepository;
    private final AdminCategoryMediumRepository mediumRepository;
    private final CardRepository cardRepository;
    private final AuctionRepository auctionRepository;

    /**
     * 대분류 생성
     * <P>
     * 이름 중복 등 정책 검증 후 저장합니다.
     * </P>
     * 
     * @PARAM request 생성 요청
     * @RETURN CategoryMajorResponse
     */
    @Transactional
    public CategoryMajorResponse createMajor(CategoryMajorCreateRequest request) {
        // 대분류명 중복 검사
        if (majorRepository.existsByName(request.getName())) {
            throw new BaseException(BaseResponseStatus.CATEGORY_MAJOR_NAME_DUPLICATED);
        }
        CategoryMajor major = new CategoryMajor();
        major.setName(request.getName());
        major.setDescription(request.getDescription());
        CategoryMajor saved = majorRepository.save(major);
        return CategoryMajorResponse.from(saved);
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
    @Transactional
    public CategoryMajorResponse updateMajor(Long majorId, CategoryMajorUpdateRequest request) {
        if ((request.getName() == null) && (request.getDescription() == null)) {
            throw new BaseException(BaseResponseStatus.INVALID_PARAMETER);
        }

        CategoryMajor major = majorRepository.findById(majorId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));

        // 이름 변경 시 중복 검사
        if (request.getName() != null && majorRepository.existsByNameAndIdNot(request.getName(), majorId)) {
            throw new BaseException(BaseResponseStatus.CATEGORY_MAJOR_NAME_DUPLICATED);
        }

        if (request.getName() != null) {
            major.setName(request.getName());
        }
        if (request.getDescription() != null) {
            major.setDescription(request.getDescription());
        }
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
    @Transactional
    public void deleteMajor(Long majorId) {
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
        majorRepository.delete(major);
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
    @Transactional
    public CategoryMediumResponse createMedium(CategoryMediumCreateRequest request) {
        CategoryMajor major = majorRepository.findById(request.getMajorId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));

        // 동일 대분류 내 중분류명 중복 검사
        if (mediumRepository.existsByCategoryMajor_IdAndName(request.getMajorId(), request.getName())) {
            throw new BaseException(BaseResponseStatus.CATEGORY_MEDIUM_NAME_DUPLICATED);
        }

        CategoryMedium medium = new CategoryMedium();
        medium.setCategoryMajor(major);
        medium.setName(request.getName());
        medium.setDescription(request.getDescription());
        CategoryMedium saved = mediumRepository.save(medium);
        return CategoryMediumResponse.from(saved);
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
    @Transactional
    public CategoryMediumResponse updateMedium(Long mediumId, CategoryMediumUpdateRequest request) {
        if ((request.getName() == null) && (request.getDescription() == null)) {
            throw new BaseException(BaseResponseStatus.INVALID_PARAMETER);
        }

        CategoryMedium medium = mediumRepository.findById(mediumId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));

        Long targetMajorId = (request.getMajorId() != null) ? request.getMajorId() : medium.getCategoryMajor().getId();

        if (request.getMajorId() != null && !Objects.equals(medium.getCategoryMajor().getId(), request.getMajorId())) {
            // 상위 대분류 변경 검증
            majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_PARENT_NOT_FOUND));
        }

        // 이름 변경(또는 상위 변경) 시 자기 자신 제외 중복 검사
        if (request.getName() != null && mediumRepository.existsByCategoryMajor_IdAndNameAndIdNot(targetMajorId,
                request.getName(), mediumId)) {
            throw new BaseException(BaseResponseStatus.CATEGORY_MEDIUM_NAME_DUPLICATED);
        }

        if (request.getMajorId() != null && !Objects.equals(medium.getCategoryMajor().getId(), request.getMajorId())) {
            CategoryMajor newMajor = majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_PARENT_NOT_FOUND));
            medium.setCategoryMajor(newMajor);
        }
        if (request.getName() != null) {
            medium.setName(request.getName());
        }
        if (request.getDescription() != null) {
            medium.setDescription(request.getDescription());
        }
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
    @Transactional
    public void deleteMedium(Long mediumId) {
        CategoryMedium medium = mediumRepository.findById(mediumId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));
        // 카드/경매 참조 여부
        boolean usedByCard = cardRepository.countByCategoryMediumId(mediumId) > 0;
        boolean usedByAuction = auctionRepository.countByCategoryMediumId(mediumId) > 0;
        if (usedByCard || usedByAuction) {
            throw new BaseException(BaseResponseStatus.CATEGORY_MEDIUM_IN_USE);
        }
        mediumRepository.delete(medium);
    }
}
