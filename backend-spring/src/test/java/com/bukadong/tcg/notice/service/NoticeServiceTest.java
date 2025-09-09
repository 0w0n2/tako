package com.bukadong.tcg.notice.service;

import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.exception.BaseException;
import com.bukadong.tcg.media.entity.Media;
import com.bukadong.tcg.media.entity.MediaKind;
import com.bukadong.tcg.media.entity.MediaType;
import com.bukadong.tcg.media.repository.MediaRepository;
import com.bukadong.tcg.member.entity.Member;
import com.bukadong.tcg.notice.dto.NoticeDetailDto;
import com.bukadong.tcg.notice.dto.NoticeSummaryDto;
import com.bukadong.tcg.notice.entity.Notice;
import com.bukadong.tcg.notice.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * NoticeService 테스트
 * <p>
 * 조회수 증가, 목록/상세 DTO 매핑을 검증한다.
 * </p>
 */
class NoticeServiceTest {

    private final NoticeRepository noticeRepository = mock(NoticeRepository.class);
    private final MediaRepository mediaRepository = mock(MediaRepository.class);
    private final NoticeService noticeService = new NoticeService(noticeRepository, mediaRepository);

    @Test
    @DisplayName("getSummaryPage - 제목/닉네임/조회수/생성일 매핑")
    void getSummaryPage_mapsFields() {
        Member author = mock(Member.class);
        given(author.getId()).willReturn(10L);
        given(author.getNickname()).willReturn("관리자");

        Notice n1 = mock(Notice.class);
        given(n1.getId()).willReturn(1L);
        given(n1.getTitle()).willReturn("공지1");
        given(n1.getAuthor()).willReturn(author);
        given(n1.getViewCount()).willReturn(5L);
        given(n1.getCreatedAt()).willReturn(LocalDateTime.of(2025, 9, 9, 12, 0));

        Page<Notice> page = new PageImpl<>(List.of(n1), PageRequest.of(0, 20, Sort.by("createdAt").descending()), 1);
        given(noticeRepository.findAllBy(ArgumentMatchers.any(Pageable.class))).willReturn(page);

        Page<NoticeSummaryDto> result = noticeService.getSummaryPage(0, 20);

        assertThat(result.getTotalElements()).isEqualTo(1);
        NoticeSummaryDto dto = result.getContent().get(0);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.title()).isEqualTo("공지1");
        assertThat(dto.authorNickname()).isEqualTo("관리자");
        assertThat(dto.viewCount()).isEqualTo(5L);
        assertThat(dto.createdAt()).isEqualTo(LocalDateTime.of(2025, 9, 9, 12, 0));
    }

    @Test
    @DisplayName("getDetail - 조회수 증가 + 첨부 포함 반환")
    void getDetail_incrementsViewCount_andReturnsAttachments() {
        long noticeId = 7L;

        given(noticeRepository.incrementViewCount(noticeId)).willReturn(1);

        Member author = mock(Member.class);
        given(author.getId()).willReturn(3L);
        given(author.getNickname()).willReturn("관리자");

        Notice notice = mock(Notice.class);
        given(notice.getId()).willReturn(noticeId);
        given(notice.getTitle()).willReturn("제목");
        given(notice.getText()).willReturn("내용");
        given(notice.getAuthor()).willReturn(author);
        given(notice.getViewCount()).willReturn(11L);
        given(notice.getCreatedAt()).willReturn(LocalDateTime.of(2025, 9, 9, 10, 0));
        given(notice.getUpdatedAt()).willReturn(LocalDateTime.of(2025, 9, 9, 11, 0));

        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

        Media m1 = Media.builder().id(100L).type(MediaType.NOTICE_ATTACHMENT).ownerId(noticeId)
                .url("https://cdn.example.com/a.pdf").mediaKind(MediaKind.IMAGE).mimeType("application/pdf").seqNo(1)
                .build();
        given(mediaRepository.findByTypeAndOwnerIdOrderBySeqNoAsc(MediaType.NOTICE_ATTACHMENT, noticeId))
                .willReturn(List.of(m1));

        NoticeDetailDto dto = noticeService.getDetail(noticeId);

        then(noticeRepository).should().incrementViewCount(noticeId);
        then(noticeRepository).should().findById(noticeId);
        then(mediaRepository).should().findByTypeAndOwnerIdOrderBySeqNoAsc(MediaType.NOTICE_ATTACHMENT, noticeId);

        assertThat(dto.id()).isEqualTo(noticeId);
        assertThat(dto.title()).isEqualTo("제목");
        assertThat(dto.text()).isEqualTo("내용");
        assertThat(dto.authorId()).isEqualTo(3L);
        assertThat(dto.authorNickname()).isEqualTo("관리자");
        assertThat(dto.viewCount()).isEqualTo(11L);
        assertThat(dto.attachments()).hasSize(1);
        assertThat(dto.attachments().get(0).url()).isEqualTo("https://cdn.example.com/a.pdf");
    }

    @Test
    @DisplayName("getDetail - 존재하지 않으면 NOT_FOUND")
    void getDetail_notFound() {
        long invalidId = 999L;
        given(noticeRepository.incrementViewCount(invalidId)).willReturn(0);

        assertThatThrownBy(() -> noticeService.getDetail(invalidId)).isInstanceOf(BaseException.class)
                .extracting("status").isEqualTo(BaseResponseStatus.NOT_FOUND);

        then(noticeRepository).should().incrementViewCount(invalidId);
        then(noticeRepository).should(never()).findById(anyLong());
    }

    @Test
    @DisplayName("getSummaryPage - size 캡 100, page 음수→0, sort createdAt DESC")
    void getSummaryPage_capsSizeAndNonNegativePage_andSort() {
        Page<Notice> empty = new PageImpl<>(List.of());
        given(noticeRepository.findAllBy(any(Pageable.class))).willReturn(empty);

        noticeService.getSummaryPage(-5, 1000);

        var captor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        then(noticeRepository).should().findAllBy(captor.capture());

        Pageable p = captor.getValue();
        assertThat(p.getPageNumber()).isEqualTo(0);
        assertThat(p.getPageSize()).isEqualTo(100);
        Sort.Order order = p.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
