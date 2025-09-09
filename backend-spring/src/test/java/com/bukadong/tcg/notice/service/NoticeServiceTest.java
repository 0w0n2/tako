package com.bukadong.tcg.notice.service;

import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.exception.BaseException;
import com.bukadong.tcg.notice.dto.response.NoticeDetailDto;
import com.bukadong.tcg.notice.dto.response.NoticeSummaryDto;
import com.bukadong.tcg.notice.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * NoticeService (QueryDSL 적용) 단위 테스트
 * <p>
 * Repository가 DTO를 직접 반환하는 흐름과 조회수 증가 → 상세 조회 호출 순서를 검증한다.
 * </p>
 */
class NoticeServiceTest {

    private final NoticeRepository noticeRepository = mock(NoticeRepository.class);
    private final NoticeViewCounterService viewCounterService = mock(
            NoticeViewCounterService.class);

    // ✅ 현재 서비스 시그니처에 맞게 2개만 주입
    private final NoticeService noticeService = new NoticeService(noticeRepository,
            viewCounterService);

    /**
     * getSummaryPage - Repository가 DTO 페이지를 직접 반환
     * <p>
     * createdAt DESC 정렬로 조회되며, DTO 필드 매핑을 검증한다.
     * </p>
     * 
     * @return 없음
     * @param 없음
     */
    @Test
    @DisplayName("getSummaryPage - Repository가 DTO 페이지를 직접 반환")
    void getSummaryPage_returnsDtoPageFromRepository() {
        // given
        NoticeSummaryDto s1 = new NoticeSummaryDto(1L, "공지1", "관리자", 5L,
                LocalDateTime.of(2025, 9, 9, 12, 0));
        Page<NoticeSummaryDto> page = new PageImpl<>(List.of(s1),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")), 1);
        given(noticeRepository.findSummaryPage(any(Pageable.class))).willReturn(page);

        // when
        Page<NoticeSummaryDto> result = noticeService.getSummaryPage(0, 20);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        NoticeSummaryDto dto = result.getContent().get(0);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.title()).isEqualTo("공지1");
        assertThat(dto.authorNickname()).isEqualTo("관리자");
        assertThat(dto.viewCount()).isEqualTo(5L);
        assertThat(dto.createdAt()).isEqualTo(LocalDateTime.of(2025, 9, 9, 12, 0));
    }

    /**
     * getSummaryPage - size 최대 100, page 음수 → 0, createdAt DESC 정렬
     * <p>
     * 페이지 파라미터 안전화와 정렬 구성을 검증한다.
     * </p>
     * 
     * @return 없음
     * @param 없음
     */
    @Test
    @DisplayName("getSummaryPage - size 최대 100, page 음수→0, createdAt DESC 정렬")
    void getSummaryPage_capsSizeAndNonNegativePage_andSort() {
        // given
        given(noticeRepository.findSummaryPage(any(Pageable.class))).willReturn(Page.empty());

        // when
        noticeService.getSummaryPage(-5, 1000);

        // then
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        then(noticeRepository).should().findSummaryPage(captor.capture());

        Pageable p = captor.getValue();
        assertThat(p.getPageNumber()).isEqualTo(0);
        assertThat(p.getPageSize()).isEqualTo(100);
        Sort.Order order = p.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    /**
     * getDetail - 조회수 증가 후 상세 DTO 반환 (호출 순서 보장)
     * <p>
     * 조회수 증가가 먼저 수행되고, 이후 상세 조회가 호출되는지 검증한다.
     * </p>
     * 
     * @return 없음
     * @param 없음
     */
    @Test
    @DisplayName("getDetail - 조회수 증가 후 상세 DTO 반환 (호출 순서 보장)")
    void getDetail_incrementsViewCount_thenReturnsDetail() {
        // given
        long id = 7L;
        willDoNothing().given(viewCounterService).increment(id);

        NoticeDetailDto detail = new NoticeDetailDto(id, "제목", "내용", 3L, "관리자", 11L,
                LocalDateTime.of(2025, 9, 9, 10, 0), LocalDateTime.of(2025, 9, 9, 11, 0), List.of() // attachments
        );
        given(noticeRepository.findDetailDtoById(id)).willReturn(detail);

        // when
        NoticeDetailDto result = noticeService.getDetail(id);

        // then
        InOrder inOrder = inOrder(viewCounterService, noticeRepository);
        inOrder.verify(viewCounterService).increment(id);
        inOrder.verify(noticeRepository).findDetailDtoById(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.title()).isEqualTo("제목");
        assertThat(result.viewCount()).isEqualTo(11L);
    }

    /**
     * getDetail - 상세 조회 시 NOT_FOUND 예외 전파
     * <p>
     * 레포지토리가 NOT_FOUND를 던지면 서비스가 그대로 전파하는지 검증한다.
     * </p>
     * 
     * @return 없음
     * @param 없음
     */
    @Test
    @DisplayName("getDetail - 상세 조회 시 NOT_FOUND 예외 전파")
    void getDetail_notFound() {
        // given
        long invalidId = 999L;
        willDoNothing().given(viewCounterService).increment(invalidId);
        given(noticeRepository.findDetailDtoById(invalidId))
                .willThrow(new BaseException(BaseResponseStatus.NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> noticeService.getDetail(invalidId))
                .isInstanceOf(BaseException.class).extracting("status")
                .isEqualTo(BaseResponseStatus.NOT_FOUND);

        InOrder inOrder = inOrder(viewCounterService, noticeRepository);
        inOrder.verify(viewCounterService).increment(invalidId);
        inOrder.verify(noticeRepository).findDetailDtoById(invalidId);
    }
}
