package com.bukadong.tcg.notice.repository;

import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.exception.BaseException;
import com.bukadong.tcg.media.dto.MediaDto;
import com.bukadong.tcg.media.entity.MediaType;
import com.bukadong.tcg.media.entity.QMedia;
import com.bukadong.tcg.member.entity.QMember;
import com.bukadong.tcg.notice.dto.response.NoticeDetailDto;
import com.bukadong.tcg.notice.dto.response.NoticeSummaryDto;
import com.bukadong.tcg.notice.entity.Notice;
import com.bukadong.tcg.notice.entity.QNotice;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import static com.bukadong.tcg.notice.entity.QNotice.notice;

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

    @Override
    public Page<NoticeSummaryDto> findSummaryPage(Pageable pageable) {
        List<NoticeSummaryDto> content = queryFactory
                .select(Projections.constructor(NoticeSummaryDto.class, QNotice.notice.id,
                        QNotice.notice.title, QNotice.notice.author.nickname,
                        QNotice.notice.viewCount, QNotice.notice.createdAt))
                .from(QNotice.notice).leftJoin(QNotice.notice.author, QMember.member)
                .orderBy(QNotice.notice.createdAt.desc()).offset(pageable.getOffset())
                .limit(pageable.getPageSize()).fetch();

        Long total = queryFactory.select(QNotice.notice.count()).from(QNotice.notice).fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public NoticeDetailDto findDetailDtoById(Long id) {
        // 공지사항 + 작성자 조회
        Notice noticeEntity = queryFactory.selectFrom(QNotice.notice)
                .leftJoin(QNotice.notice.author, QMember.member).fetchJoin()
                .where(QNotice.notice.id.eq(id)).fetchOne();

        if (noticeEntity == null) {
            throw new BaseException(BaseResponseStatus.NOT_FOUND);
        }

        // 첨부파일 조회
        List<MediaDto> attachments = queryFactory
                .select(Projections.constructor(MediaDto.class, QMedia.media.id, QMedia.media.url,
                        QMedia.media.mediaKind, QMedia.media.mimeType, QMedia.media.seqNo))
                .from(QMedia.media)
                .where(QMedia.media.type.eq(MediaType.NOTICE_ATTACHMENT)
                        .and(QMedia.media.ownerId.eq(id)))
                .orderBy(QMedia.media.seqNo.asc()).fetch();

        return NoticeDetailDto.of(noticeEntity, attachments);
    }

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
