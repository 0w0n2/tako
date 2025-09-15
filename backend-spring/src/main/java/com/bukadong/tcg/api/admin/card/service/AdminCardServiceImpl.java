package com.bukadong.tcg.api.admin.card.service;

import com.bukadong.tcg.api.admin.card.dto.response.CreateCardRequestDto;
import com.bukadong.tcg.api.card.entity.Card;
import com.bukadong.tcg.api.card.repository.CardRepository;
import com.bukadong.tcg.api.category.entity.CategoryMajor;
import com.bukadong.tcg.api.category.entity.CategoryMedium;
import com.bukadong.tcg.api.category.repository.CategoryMajorRepository;
import com.bukadong.tcg.api.category.repository.CategoryMediumRepository;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaAttachmentService;
import com.bukadong.tcg.api.media.util.MediaDirResolver;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCardServiceImpl implements AdminCardService {

    private final CardRepository cardRepository;
    private final CategoryMajorRepository categoryMajorRepository;
    private final CategoryMediumRepository categoryMediumRepository;
    private final MediaAttachmentService mediaAttachmentService;
    private final MediaDirResolver mediaDirResolver;

    /* 신규 카드 등록 */
    @Override
    public void saveCard(CreateCardRequestDto requestDto, MultipartFile cardImage) {
        // 1. 카드 이름 중복 확인
        if (cardRepository.existsByName(requestDto.name())) {
            throw new BaseException(BaseResponseStatus.CARD_NAME_DUPLICATED);
        }

        // 2. 대분류 조회
        CategoryMajor categoryMajor = categoryMajorRepository.findById(requestDto.categoryMajorId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_BAD_REQUEST));

        // 3. 중분류 조회
        CategoryMedium categoryMedium = categoryMediumRepository.findById(requestDto.categoryMediumId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_BAD_REQUEST));

        // 4. 대분류 - 중분류 연관관계 검증
        if (!categoryMedium.getCategoryMajor().getId().equals(categoryMajor.getId())) {
            throw new BaseException(BaseResponseStatus.CATEGORY_PARENT_NOT_FOUND);
        }

        // 5. 카드 등록
        Card card = requestDto.toCard(categoryMajor, categoryMedium);
        Card savedCard = cardRepository.save(card);

        // 6. 카드 이미지 등록
        mediaAttachmentService.addByMultipart(MediaType.CARD, savedCard.getId(), null, List.of(cardImage), mediaDirResolver.resolve(MediaType.CARD));
    }
}
