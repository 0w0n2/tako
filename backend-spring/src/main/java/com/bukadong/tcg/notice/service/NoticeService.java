package com.bukadong.tcg.notice.service;

import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.exception.BaseException;
import com.bukadong.tcg.media.entity.Media;
import com.bukadong.tcg.media.entity.MediaType;
import com.bukadong.tcg.media.repository.MediaRepository;
import com.bukadong.tcg.notice.dto.response.NoticeAttachmentDto;
import com.bukadong.tcg.notice.dto.response.NoticeDetailDto;
import com.bukadong.tcg.notice.dto.response.NoticeSummaryDto;
import com.bukadong.tcg.notice.entity.Notice;
import com.bukadong.tcg.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공지사항 조회 비즈니스 로직.
 * <p>
 * 레포지토리에서 엔티티/미디어를 조회한 뒤 DTO로 변환하여 반환한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 트랜잭션
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MediaRepository mediaRepository;
    private final NoticeViewCounterService viewCounterService;

    /**
     * 공지사항 단건 조회
     * <p>
     * 조회수는 별도 쓰기 트랜잭션(REQUIRES_NEW)에서 증가시키고, 이후 읽기 트랜잭션에서 최신 상태를 로드한다.
     * </p>
     *
     * @param page 0부터 시작하는 페이지 번호
     * @param size 페이지 크기(최대 100)
     * @return 공지사항 요약 DTO 페이지
     */
    public Page<NoticeSummaryDto> getSummaryPage(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notice> entities = noticeRepository.findAllBy(pageable);
        return entities.map(NoticeSummaryDto::from);
    }

    /**
     * 공지사항 단건 조회 조회수를 1 증가시킨 뒤, 작성자 포함 공지를 조회하고 첨부파일(Media: NOTICE_ATTACHMENT)
     * </p>
     * 
     * @param id 공지사항 ID
     * @return 공지사항 상세 DTO
     * @throws BaseException NOT_FOUND: 존재하지 않을 때
     */
    @Transactional
    public NoticeDetailDto getDetail(Long id) {
        // 조회수 1 증가 (쓰기 트랜잭션)
        viewCounterService.increment(id);

        // 공지(작성자 포함) 조회 (읽기 트랜잭션)
        Notice n = noticeRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        // 첨부파일 조회 (Media 테이블)
        List<Media> medias = mediaRepository
                .findByTypeAndOwnerIdOrderBySeqNoAsc(MediaType.NOTICE_ATTACHMENT, id);
        List<NoticeAttachmentDto> files = medias.stream().map(NoticeAttachmentDto::from).toList();
        return NoticeDetailDto.of(n, files);
    }
}
