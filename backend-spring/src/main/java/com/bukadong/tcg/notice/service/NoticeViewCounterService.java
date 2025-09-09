// 파일:
// src/main/java/com/bukadong/tcg/notice/service/NoticeViewCounterService.java

package com.bukadong.tcg.notice.service;

import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.exception.BaseException;
import com.bukadong.tcg.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공지 조회수 증가 서비스
 * <p>
 * 공지사항의 조회수를 DB에서 원자적으로 +1 증가시킨다. 작은 쓰기 트랜잭션(REQUIRES_NEW)으로 분리하여 읽기 트랜잭션과의 격리를
 * 보장하고, 이후 조회에서 최신 값이 보이도록 커밋 타이밍을 분리한다.
 * </p>
 *
 * @param 없음
 * @return 없음
 */
@Service
@RequiredArgsConstructor
public class NoticeViewCounterService {

    private final NoticeRepository noticeRepository;

    /**
     * 조회수 증가
     * <p>
     * 단일 UPDATE 쿼리로 viewCount를 1 증가시킨다. 영향 행이 0이면 대상이 없으므로 NOT_FOUND 예외를 던진다.
     * </p>
     *
     * @param id 공지 ID
     * @return 없음
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increment(Long id) {
        int updated = noticeRepository.incrementViewCount(id);
        if (updated == 0) {
            throw new BaseException(BaseResponseStatus.NOT_FOUND);
        }
    }
}
