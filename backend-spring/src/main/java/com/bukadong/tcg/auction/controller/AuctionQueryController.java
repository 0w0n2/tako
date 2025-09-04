package com.bukadong.tcg.auction.controller;

import com.bukadong.tcg.auction.dto.AuctionDto;
import com.bukadong.tcg.auction.entity.Auction;
import com.bukadong.tcg.auction.service.AuctionQueryService;
import com.bukadong.tcg.common.base.*;
import com.bukadong.tcg.common.exception.*;
import com.bukadong.tcg.media.dto.MediaDto;
import com.bukadong.tcg.media.entity.Media;
import com.bukadong.tcg.media.entity.MediaType;
import com.bukadong.tcg.media.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 경매 조회 API (공개)
 *
 * <p>경매 목록, 상세, 미디어(이미지/영상) 정보를 조회할 수 있다.</p>
 * <ul>
 *   <li>{@code GET /api/v1/auctions} - 경매 목록 조회 (상태/카드 필터 + 페이징)</li>
 *   <li>{@code GET /api/v1/auctions/{id}} - 경매 상세 조회</li>
 *   <li>{@code GET /api/v1/auctions/{id}/media} - 경매 미디어 목록 조회</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class AuctionQueryController {

    private final AuctionQueryService auctionQueryService;
    private final com.bukadong.tcg.auction.repository.AuctionRepository auctionRepository;
    private final MediaRepository mediaRepository;

    /**
     * 경매 목록 조회
     *
     * <p>경매 상태, 카드 ID를 기준으로 필터링하며 페이징 처리된 결과를 반환한다.</p>
     *
     * @param status 경매 상태 필터 (예: RUNNING, ENDED)
     * @param cardId 특정 카드 ID로 필터링할 경우 지정
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기 (기본 20)
     * @return 경매 목록(Page) → AuctionDto 변환 결과
     *
     * @see AuctionQueryService#search(AuctionQueryService.Status, Long, org.springframework.data.domain.Pageable)
     */
    @GetMapping
    public BaseResponse<?> list(
            @RequestParam(required = false) AuctionQueryService.Status status,
            @RequestParam(required = false) Long cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Auction> p = auctionQueryService.search(status, cardId, PageRequest.of(page, size));
        Page<AuctionDto> mapped = p.map(AuctionDto::of);
        return new BaseResponse<>(mapped);
    }

    /**
     * 경매 상세 조회
     *
     * <p>경매 ID를 통해 단일 경매 상세 정보를 조회한다.</p>
     *
     * @param id 경매 ID (PK)
     * @return AuctionDto 형태의 상세 정보
     * @throws BaseException NOT_FOUND - 해당 ID의 경매가 없을 경우
     */
    @GetMapping("/{id}")
    public BaseResponse<?> get(@PathVariable Long id) {
        Auction a = auctionRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        return new BaseResponse<>(AuctionDto.of(a));
    }

    /**
     * 경매 미디어 목록 조회
     *
     * <p>특정 경매에 등록된 미디어(이미지, 동영상)를 순서대로 반환한다.</p>
     *
     * @param id 경매 ID
     * @return MediaDto 리스트 (seqNo 순서)
     */
    @GetMapping("/{id}/media")
    public BaseResponse<?> media(@PathVariable Long id) {
        List<Media> list = mediaRepository.findByTypeAndOwnerIdOrderBySeqNoAsc(MediaType.AUCTION, id);
        return new BaseResponse<>(list.stream().map(MediaDto::of).toList());
    }
}
