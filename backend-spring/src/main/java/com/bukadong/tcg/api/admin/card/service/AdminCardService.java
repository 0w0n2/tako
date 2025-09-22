package com.bukadong.tcg.api.admin.card.service;

import com.bukadong.tcg.api.admin.card.dto.response.CreateCardRequestDto;
import com.bukadong.tcg.api.card.entity.Card;
import com.bukadong.tcg.api.card.repository.CardRepository;
import com.bukadong.tcg.api.category.entity.CategoryMedium;
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
public class AdminCardService {

    private final CardRepository cardRepository;
    private final CategoryMediumRepository categoryMediumRepository;
    private final MediaAttachmentService mediaAttachmentService;
    private final MediaDirResolver mediaDirResolver;

    /* 신규 카드 등록 */
    public void saveCard(CreateCardRequestDto requestDto, MultipartFile cardImage) {
        // 1. 카드 이름 중복 확인
        if (cardRepository.existsByName(requestDto.name())) {
            throw new BaseException(BaseResponseStatus.CARD_NAME_DUPLICATED);
        }

        // 2. 중분류 ID로 중분류 & 대분류 조회
        CategoryMedium categoryMedium = categoryMediumRepository.findWithMajorById(requestDto.categoryMediumId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_BAD_REQUEST));

        // 3. 카드 등록
        Card card = requestDto.toCard(categoryMedium.getCategoryMajor(), categoryMedium);
        Card savedCard = cardRepository.save(card);

        // 4. 카드 이미지 등록
        mediaAttachmentService.addByMultipart(MediaType.CARD, savedCard.getId(), null, List.of(cardImage), mediaDirResolver.resolve(MediaType.CARD));
    }
}
