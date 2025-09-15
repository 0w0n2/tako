package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.converter.AuctionBidUnitConverter;
import com.bukadong.tcg.api.auction.dto.request.AuctionCreateRequest;
import com.bukadong.tcg.api.auction.dto.response.AuctionCreateResponse;
import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionBidUnit;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.card.entity.Card;
import com.bukadong.tcg.api.card.entity.CardAiGrade;
import com.bukadong.tcg.api.card.repository.CardRepository;
import com.bukadong.tcg.api.card.repository.CardAiGradeRepository;
import com.bukadong.tcg.api.category.entity.CategoryMajor;
import com.bukadong.tcg.api.category.entity.CategoryMedium;
import com.bukadong.tcg.api.category.repository.CategoryMajorRepository;
import com.bukadong.tcg.api.category.repository.CategoryMediumRepository;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaAttachmentService;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 경매 생성 서비스
 * <P>
 * 1) gradeHash로 CardAiGrade 조회 → 2) (옵션) 실물카드 매핑 → 3) Auction 저장 → 4) 이미지 첨부.
 * </P>
 * 
 * @PARAM request 경매 생성 요청, memberId 생성자 회원 ID, files 이미지 파일(옵션)
 * @RETURN AuctionCreateResponse
 */
// 변경 이후 코드의 코드블럭만

@Service
@RequiredArgsConstructor
public class AuctionCommandService {

    private final AuctionRepository auctionRepository;
    private final CardRepository cardRepository;
    // PhysicalCard 미사용
    private final CardAiGradeRepository cardAiGradeRepository;
    private final CategoryMajorRepository categoryMajorRepository;
    private final CategoryMediumRepository categoryMediumRepository;
    private final MediaAttachmentService mediaAttachmentService;

    /**
     * 경매 생성 서비스
     * <P>
     * 1) gradeHash로 CardAiGrade 조회 → 2) Auction 저장 → 3) 이미지 첨부(addByMultipart).
     * </P>
     * 
     * @PARAM request 경매 생성 요청
     * @PARAM memberId 생성자 회원 ID
     * @PARAM files 이미지 파일(옵션)
     * @PARAM dir MediaDirResolver로부터 계산된 업로드 디렉토리 (예: "auction")
     * @RETURN AuctionCreateResponse(auctionId만)
     */
    @Transactional
    public AuctionCreateResponse create(AuctionCreateRequest request, Member me, List<MultipartFile> files,
            String dir) {

        // 필수 엔티티 로드 (DB 의존 검증)
        CardAiGrade grade = cardAiGradeRepository.findByHash(request.getGradeHash())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AUCTION_GRADE_NOT_FOUND));

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AUCTION_CARD_NOT_FOUND));

        CategoryMajor major = categoryMajorRepository.findById(request.getCategoryMajorId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AUCTION_CATEGORY_MAJOR_NOT_FOUND));

        CategoryMedium medium = categoryMediumRepository.findById(request.getCategoryMediumId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AUCTION_CATEGORY_MEDIUM_NOT_FOUND));

        // Auction 생성/저장 (PhysicalCard는 현재 미구현 → null)
        AuctionBidUnit bidUnit = AuctionBidUnit.fromValue(request.getBidUnit());
        if (request.getEndDatetime().isBefore(request.getStartDatetime())) {
            throw new BaseException(BaseResponseStatus.AUCTION_DATE_INVALID);
        }
        int durationDays = Math.max(1,
                (int) java.time.Duration.between(request.getStartDatetime(), request.getEndDatetime()).toDays());

        Auction auction = Auction.builder().member(me).delivery(null).physicalCard(null) // ← 명시적으로 null
                .card(card).categoryMajor(major).categoryMedium(medium).grade(grade)
                .code(java.util.UUID.randomUUID().toString()).title(request.getTitle()).detail(request.getDetail())
                .startPrice(request.getStartPrice())
                .currentPrice(java.util.Optional.ofNullable(request.getCurrentPrice()).orElse(request.getStartPrice()))
                .bidUnit(bidUnit).startDatetime(request.getStartDatetime()).endDatetime(request.getEndDatetime())
                .durationDays(durationDays).isEnd(false).buyNowFlag(request.isBuyNowFlag())
                .buyNowPrice(request.getBuyNowPrice()).extensionFlag(true).taxFlag(false).build();

        Auction saved = auctionRepository.save(auction);

        // 3) 이미지 첨부 (Media API 방식과 동일): addByMultipart 사용
        if (files != null && !files.isEmpty()) {
            mediaAttachmentService.addByMultipart(MediaType.AUCTION_ITEM, saved.getId(), me, files, dir);
        }

        // 응답은 id만
        return AuctionCreateResponse.builder().auctionId(saved.getId()).build();
    }
}
