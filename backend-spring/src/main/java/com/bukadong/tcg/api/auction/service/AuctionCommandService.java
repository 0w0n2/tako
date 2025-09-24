package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.dto.request.AuctionCreateRequest;
import com.bukadong.tcg.api.auction.dto.response.AuctionCreateResponse;
import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.repository.AuctionRepositoryCustom;
import com.bukadong.tcg.api.auction.repository.AuctionResultRepository;
import com.bukadong.tcg.api.auction.util.AuctionDeadlineIndex;
import com.bukadong.tcg.api.bid.entity.AuctionBidUnit;
import com.bukadong.tcg.api.card.entity.Card;
import com.bukadong.tcg.api.card.entity.CardAiGrade;
import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.card.repository.CardRepository;
import com.bukadong.tcg.api.card.repository.CardAiGradeRepository;
import com.bukadong.tcg.api.card.repository.PhysicalCardRepository;
import com.bukadong.tcg.api.category.entity.CategoryMajor;
import com.bukadong.tcg.api.category.entity.CategoryMedium;
import com.bukadong.tcg.api.category.repository.CategoryMajorRepository;
import com.bukadong.tcg.api.category.repository.CategoryMediumRepository;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaAttachmentService;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.blockchain.service.TakoNftContractService;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.bukadong.tcg.api.notification.service.NotificationCommandService;
import com.bukadong.tcg.api.wish.repository.WishQueryPort;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 경매 생성 서비스
 * <p>
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
    private final CardAiGradeRepository cardAiGradeRepository;
    private final CategoryMajorRepository categoryMajorRepository;
    private final CategoryMediumRepository categoryMediumRepository;
    private final MediaAttachmentService mediaAttachmentService;
    private final NotificationCommandService notificationCommandService;
    private final WishQueryPort wishQueryPort;
    private final AuctionDeadlineIndex deadlineIndex;
    private final Logger logger = LoggerFactory.getLogger(AuctionCommandService.class);
    private final PhysicalCardRepository physicalCardRepository;
    private final TakoNftContractService takoNftContractService;
    private final AuctionRepositoryCustom auctionRepositoryCustom;

    /**
     * 경매 생성 가능 여부 조회, 사용자 계정에 지갑 주소가 등록되어 있어야 한다.
     */
    @Transactional(readOnly = true)
    public PhysicalCard isValidToCreateAndGetPhysicalCard(Member me, Long tokenId) {
        if (!StringUtils.hasText(me.getWalletAddress())) {
            throw new BaseException(BaseResponseStatus.WALLET_ADDRESS_NOT_FOUND);
        }

        PhysicalCard nftPhysicalCard = null;
        if (tokenId != null) {
            // 같은 NFT 카드로 진행 중인 경매가 있는지 체크
            if (auctionRepositoryCustom.isDuplicatedTokenId(tokenId)) {
                throw new BaseException(BaseResponseStatus.PHYSICAL_CARD_IS_BEING_SOLD);
            }

            // NFT 등록 경매일 때, NFT 토큰의 소유주가 사용자와 일치하는지 확인
            nftPhysicalCard = physicalCardRepository.findByTokenId(BigInteger.valueOf(tokenId))
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.PHYSICAL_CARD_NOT_FOUND));
            if (!me.getWalletAddress().equalsIgnoreCase(takoNftContractService.getOwnerAddress(tokenId))) {
                throw new BaseException(BaseResponseStatus.PHYSICAL_CARD_OWNER_INVALID);
            }
        }

        return nftPhysicalCard;
    }

    /**
     * 경매 생성 서비스
     * <p>
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
    public AuctionCreateResponse create(AuctionCreateRequest requestDto, Member me, List<MultipartFile> files,
                                        String dir, PhysicalCard nftPhysicalCard) {
        // 필수 엔티티 로드 (DB 의존 검증)
        CardAiGrade grade = cardAiGradeRepository.findByHash(requestDto.getGradeHash())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AUCTION_GRADE_NOT_FOUND));

        Card card = cardRepository.findById(requestDto.getCardId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AUCTION_CARD_NOT_FOUND));

        CategoryMajor major = categoryMajorRepository.findById(requestDto.getCategoryMajorId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AUCTION_CATEGORY_MAJOR_NOT_FOUND));

        CategoryMedium medium = categoryMediumRepository.findById(requestDto.getCategoryMediumId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AUCTION_CATEGORY_MEDIUM_NOT_FOUND));

        // 클라이언트는 KST(LocalDateTime)로 보냄 → 서버에서 UTC로 변환해 저장/처리
        final ZoneId kst = ZoneId.of("Asia/Seoul");
        LocalDateTime startUtc = requestDto.getStartDatetime().atZone(kst).withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
        LocalDateTime endUtc = requestDto.getEndDatetime().atZone(kst).withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        // Auction 생성/저장 (PhysicalCard는 현재 미구현 → null)
        AuctionBidUnit bidUnit = AuctionBidUnit.fromValue(requestDto.getBidUnit());
        if (endUtc.isBefore(startUtc)) {
            throw new BaseException(BaseResponseStatus.AUCTION_DATE_INVALID);
        }
        int durationDays = Math.max(1, (int) java.time.Duration.between(startUtc, endUtc).toDays());

        Auction auction = Auction.builder().member(me).delivery(null).physicalCard(nftPhysicalCard)
                .card(card).categoryMajor(major).categoryMedium(medium).grade(grade).code(UUID.randomUUID().toString())
                .title(requestDto.getTitle()).detail(requestDto.getDetail()).startPrice(requestDto.getStartPrice())
                .currentPrice(Optional.ofNullable(requestDto.getCurrentPrice()).orElse(requestDto.getStartPrice()))
                .bidUnit(bidUnit).startDatetime(startUtc).endDatetime(endUtc).durationDays(durationDays).isEnd(false)
                .buyNowFlag(requestDto.isBuyNowFlag()).buyNowPrice(requestDto.getBuyNowPrice()).extensionFlag(true)
                .taxFlag(false).build();

        Auction saved = auctionRepository.save(auction);

        // 3) 이미지 첨부 (Media API 방식과 동일): addByMultipart 사용
        if (files != null && !files.isEmpty()) {
            mediaAttachmentService.addByMultipart(MediaType.AUCTION_ITEM, saved.getId(), me, files, dir);
        }

        // 알림 트리거: 카드 위시한 회원들에게 알림 발송
        wishQueryPort.findMemberIdsWhoWishedCard(card.getId()).stream().filter(mid -> !mid.equals(me.getId()))
                .forEach(mid -> notificationCommandService.notifyWishCardListed(mid, saved.getId(),
                        Map.of("auctionId", saved.getId())));

        // 모든 시간 처리를 UTC 기준으로 일관화
        Instant endAt = auction.getEndDatetime().atOffset(ZoneOffset.UTC).toInstant();
        deadlineIndex.upsert(saved.getId(), endAt.toEpochMilli());
        logger.info(
                "Auction created (UTC): id={}, startKST={}, endKST={}, startUTC(LocalDateTime)={}, endUTC(LocalDateTime)={}, endAtInstantUTC={}, epochMillisUTC={}",
                saved.getId(), requestDto.getStartDatetime(), requestDto.getEndDatetime(), startUtc, endUtc, endAt,
                endAt.toEpochMilli());

        // 응답은 id만
        return AuctionCreateResponse.builder().auctionId(saved.getId()).build();
    }
}
