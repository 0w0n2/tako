package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.entity.Auction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 경매 락 전용 리포지토리
 * <P>
 * 경매 행에 대해 PESSIMISTIC_WRITE(행락)를 적용한 단건 조회를 수행한다.
 * </P>
 * 
 * @PARAM id 경매 ID
 * @RETURN Optional<Auction>
 */
@Repository
@RequiredArgsConstructor
public class AuctionLockRepository {

    private final EntityManager em;

    public Optional<Auction> findByIdForUpdate(Long id) {
        Auction found = em.find(Auction.class, id, LockModeType.PESSIMISTIC_WRITE);
        return Optional.ofNullable(found);
    }
}
