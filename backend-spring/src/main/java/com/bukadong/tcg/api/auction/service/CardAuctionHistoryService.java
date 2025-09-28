package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.dto.response.CardAuctionHistoryItemResponse;
import com.bukadong.tcg.api.auction.repository.CardAuctionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardAuctionHistoryService {

    private final CardAuctionHistoryRepository repository;

    public List<CardAuctionHistoryItemResponse> getCompletedHistories(Long cardId, Integer days) {
        int range = (days == null || days <= 0) ? 7 : days;
        return repository.findCompletedHistoriesByCardId(cardId, range);
    }
}
