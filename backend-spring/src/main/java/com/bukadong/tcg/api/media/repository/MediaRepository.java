package com.bukadong.tcg.api.media.repository;

import com.bukadong.tcg.api.media.entity.Media;
import com.bukadong.tcg.api.media.entity.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * 미디어 엔티티({@link Media})용 Spring Data JPA 레포지토리.
 * <p>
 * 기본적인 CRUD 연산은 {@link JpaRepository}를 통해 제공되며, 추가로 미디어 타입과 소유주(ownerId) 기준으로
 * 조회하는 기능을 제공한다.
 * </p>
 */
public interface MediaRepository extends JpaRepository<Media, Long> {

    /**
     * 특정 소유주 ID에 속한 미디어 목록을 조회한다.
     * <p>
     * 예: 경매(AUCTION), 리뷰(REVIEW) 등 {@link MediaType}에 따라 해당 소유주(ownerId)에 연결된 미디어를
     * 순번(seqNo) 오름차순으로 정렬해 반환한다.
     * </p>
     *
     * @param type    미디어 타입 (AUCTION, REVIEW 등)
     * @param ownerId 소유주 ID (예: 경매 ID, 리뷰 ID)
     * @return 정렬된 미디어 리스트 (대표 이미지가 먼저 옴)
     */
    List<Media> findByTypeAndOwnerIdOrderBySeqNoAsc(MediaType type, Long ownerId);

    /**
     * 카드 대표 이미지(이미지, seqNo=1)를 여러 카드 ID에 대해 한 번에 조회한다.
     * <P>
     * IN 절로 벌크 조회하여 N+1을 방지한다.
     * </P>
     * 
     * @PARAM type 미디어 타입 (CARD)
     * @PARAM ownerIds 카드 ID 목록
     * @RETURN 각 카드의 대표 Media 목록
     */
    @Query("""
                select m
                from Media m
                where m.type = :type
                  and m.seqNo = 1
                  and m.ownerId in :ownerIds
            """)
    List<Media> findCardThumbnails(@Param("type") MediaType type, @Param("ownerIds") Collection<Long> ownerIds);

}
