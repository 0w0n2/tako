package com.bukadong.tcg.notice.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.bukadong.tcg.notice.entity.QNotice.notice;

/**
 * 공지사항 커스텀 레포지토리 구현체
 * <p>
 * QueryDSL을 활용하여 공지사항 관련 커스텀 쿼리를 정의한다.
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 조회수 증가 (원자적 증가 처리)
     *
     * @param id 공지 ID
     * @return 업데이트된 행 수
     */
    @Override
    public int incrementViewCount(Long id) {
        return (int) queryFactory.update(notice).set(notice.viewCount, notice.viewCount.add(1))
                .where(notice.id.eq(id)).execute();
    }

}
