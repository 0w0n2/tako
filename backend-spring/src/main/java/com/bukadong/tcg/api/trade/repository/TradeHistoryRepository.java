package com.bukadong.tcg.api.trade.repository;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.trade.entity.TradeHistory;
import com.bukadong.tcg.api.trade.entity.TradeRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {
    boolean existsByAuctionAndRole(Auction auction, TradeRole role);
}
