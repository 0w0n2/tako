package com.bukadong.tcg.notice.service;

import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.exception.BaseException;
import com.bukadong.tcg.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공지 조회수 증가 전용 서비스
 * <p>
 * 작은 쓰기 트랜잭션으로 분리하여 락/전파 범위를 최소화한다.
 * </p>
 *
 * @param id 공지 ID
 * @return 없음
 */
@Service
@RequiredArgsConstructor
public class NoticeViewCounterService {

    private final NoticeRepository noticeRepository;

    /**
     * 조회수 증가 (쓰기 전용 트랜잭션)
     * <p>
     * 별도 트랜잭션(REQUIRES_NEW)으로 커밋하여 이후 읽기가 최신 값을 보장받도록 한다.
     * </p>
     *
     * @param id 공지 ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increment(Long id) {
        int updated = noticeRepository.incrementViewCount(id);
        if (updated == 0) {
            throw new BaseException(BaseResponseStatus.NOT_FOUND);
        }
    }
}
