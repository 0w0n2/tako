package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.sse.AuctionLiveSseService;
import com.bukadong.tcg.api.bid.service.AuctionCacheService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Auctions")
@RestController
@RequestMapping("/v1/auctions")
@RequiredArgsConstructor
public class AuctionSseController {

    private final AuctionLiveSseService sseService;
    private final AuctionCacheService auctionCacheService;

    // 전역 스트림은 사용하지 않음

    /**
     * 경매 상세 화면 SSE 구독 (특정 경매)
     */
    @GetMapping(path = "/{auctionId}/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeAuction(@PathVariable("auctionId") long auctionId) {
        // 상세는 최초 1회 Redis 스냅샷을 전송하기 위해 ensureLoaded 이후 해시 조회
        auctionCacheService.ensureLoaded(auctionId);
        Map<Object, Object> raw = sseService.readSnapshot(auctionId);
        Map<String, String> snapshot = raw.entrySet().stream()
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue())));
        return sseService.subscribeAuction(auctionId, snapshot);
    }

    /**
     * 경매 목록 화면 SSE 구독 (선택한 경매 ID들만) 예: /v1/auctions/live/select?ids=1,2,3
     */
    @GetMapping(path = "/live/select", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeSelected(@RequestParam(name = "ids", required = false) String idsParam) {
        // ids 파싱 및 정리
        Set<Long> ids = new LinkedHashSet<>();
        if (idsParam != null && !idsParam.isBlank()) {
            Arrays.stream(idsParam.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(s -> {
                try {
                    ids.add(Long.parseLong(s));
                } catch (NumberFormatException ignore) {
                    // ignore malformed id
                }
            });
        }

        // 비어있으면 전역 구독으로 대체 가능하나, 여기서는 빈 스냅샷으로 구독만 생성
        if (ids.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Query param 'ids' must contain at least one id");
        }

        // 스냅샷 수집 (필요 시 ensureLoaded)
        List<Map<String, String>> snapshots = new ArrayList<>();
        for (Long id : ids) {
            try {
                auctionCacheService.ensureLoaded(id);
                Map<Object, Object> raw = sseService.readSnapshot(id);
                Map<String, String> snap = raw.entrySet().stream()
                        .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue())));
                // 클라이언트 편의를 위해 auctionId를 명시적으로 포함
                snap = new java.util.HashMap<>(snap);
                snap.put("auctionId", String.valueOf(id));
                snapshots.add(snap);
            } catch (Exception ignore) {
                // 개별 스냅샷 실패는 건너뛰기
            }
        }
        return sseService.subscribeSelected(ids, snapshots);
    }
}
