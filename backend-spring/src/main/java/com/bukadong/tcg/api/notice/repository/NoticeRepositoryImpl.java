package com.bukadong.tcg.api.notice.repository;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaUrlService;
import com.bukadong.tcg.api.member.entity.QMember;
import com.bukadong.tcg.api.notice.dto.response.NoticeDetailDto;
import com.bukadong.tcg.api.notice.dto.response.NoticeSummaryDto;
import com.bukadong.tcg.api.notice.entity.Notice;
import com.bukadong.tcg.api.notice.entity.QNotice;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import static com.bukadong.tcg.api.notice.entity.QNotice.notice;

import java.time.Duration;
import java.util.List;

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
    private final MediaUrlService mediaUrlService;

    @Override
    public Page<NoticeSummaryDto> findSummaryPage(Pageable pageable) {
        List<NoticeSummaryDto> content = queryFactory
                .select(Projections.constructor(NoticeSummaryDto.class, QNotice.notice.id, QNotice.notice.title,
                        QNotice.notice.author.nickname, QNotice.notice.viewCount, QNotice.notice.createdAt))
                .from(QNotice.notice).leftJoin(QNotice.notice.author, QMember.member)
                .orderBy(QNotice.notice.createdAt.desc()).offset(pageable.getOffset()).limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(QNotice.notice.count()).from(QNotice.notice).fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public NoticeDetailDto findDetailDtoById(Long id) {
        // 공지사항 + 작성자 조회
        Notice noticeEntity = queryFactory.selectFrom(QNotice.notice).leftJoin(QNotice.notice.author, QMember.member)
                .fetchJoin().where(QNotice.notice.id.eq(id)).fetchOne();

        if (noticeEntity == null) {
            throw new BaseException(BaseResponseStatus.NOT_FOUND);
        }

        // 미디어 URL 조회 (5분짜리 presigned URL)
        List<String> imageUrls = mediaUrlService.getPresignedImageUrls(MediaType.NOTICE, noticeEntity.getId(),
                Duration.ofMinutes(5));
        return NoticeDetailDto.of(noticeEntity, imageUrls);
    }

    /**
     * 조회수 증가 (원자적 증가 처리)
     *
     * @param id 공지 ID
     * @return 업데이트된 행 수
     */
    @Override
    public int incrementViewCount(Long id) {
        return (int) queryFactory.update(notice).set(notice.viewCount, notice.viewCount.add(1)).where(notice.id.eq(id))
                .execute();
    }

}
