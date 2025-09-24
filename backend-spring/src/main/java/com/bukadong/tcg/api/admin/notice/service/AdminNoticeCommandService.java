package com.bukadong.tcg.api.admin.notice.service;

import com.bukadong.tcg.api.admin.notice.dto.request.NoticeCreateRequest;
import com.bukadong.tcg.api.admin.notice.dto.request.NoticeUpdateRequest;
import com.bukadong.tcg.api.admin.notice.dto.response.NoticeResponse;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaAttachmentService;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.notice.entity.Notice;
import com.bukadong.tcg.api.notification.service.NotificationCommandService;
import com.bukadong.tcg.api.notification.entity.NotificationTypeCode;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.api.notice.repository.NoticeRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 공지사항 관리자 명령 서비스
 * <P>
 * 공지 생성/수정/삭제 및 첨부(이미지/일반파일) 업로드/정리를 수행합니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AdminNoticeCommandService {

    private final NoticeRepository noticeRepository;
    private final MediaAttachmentService mediaAttachmentService;
    private final MemberRepository memberRepository;
    private final NotificationCommandService notificationCommandService;

    /**
     * 공지 생성(첨부 포함)
     * <P>
     * Notice 저장 후 이미지/첨부 파일을 업로드합니다.
     * </P>
     * 
     * @PARAM me 작성자(관리자) Member
     * @PARAM requestDto 생성 DTO
     * @PARAM images 이미지(옵션)
     * @PARAM imageDir 이미지 업로드 디렉토리(예: "notice")
     * @PARAM attachments 일반 첨부(옵션)
     * @PARAM attachDir 첨부 업로드 디렉토리(예: "notice-attach")
     * @RETURN NoticeResponse
     */
    public NoticeResponse create(Member me, NoticeCreateRequest requestDto, List<MultipartFile> images, String imageDir,
            List<MultipartFile> attachments, String attachDir) {
        Notice notice = Notice.create(me, requestDto.getTitle(), requestDto.getText());
        Notice saved = noticeRepository.save(notice);

        if (images != null && !images.isEmpty()) {
            mediaAttachmentService.addByMultipart(MediaType.NOTICE, saved.getId(), me, images, imageDir);
        }
        if (attachments != null && !attachments.isEmpty()) {
            mediaAttachmentService.addByMultipart(MediaType.NOTICE_ATTACHMENT, saved.getId(), me, attachments,
                    attachDir);
        }
        // 공지 생성 브로드캐스트 (작성자 제외)
        broadcastNoticeAsync(saved, me);
        return NoticeResponse.of(saved);
    }

    /**
     * 공지 수정(첨부 포함)
     * <P>
     * 제목/본문을 갱신하고, clear 플래그에 따라 기존 파일을 전체 삭제 후 재업로드하거나, 단순 추가합니다.
     * </P>
     * 
     * @PARAM noticeId 공지 ID
     * @PARAM requestDto 수정 DTO
     * @PARAM me 수정자(관리자) Member
     * @PARAM images 이미지(옵션)
     * @PARAM imageDir 이미지 디렉토리
     * @PARAM clearImages 기존 이미지 전체 삭제 여부
     * @PARAM attachments 일반 첨부(옵션)
     * @PARAM attachDir 첨부 디렉토리
     * @PARAM clearAttachments 기존 첨부 전체 삭제 여부
     * @RETURN NoticeResponse
     */
    public NoticeResponse update(Long noticeId, NoticeUpdateRequest requestDto, Member me, List<MultipartFile> images,
            String imageDir, boolean clearImages, List<MultipartFile> attachments, String attachDir,
            boolean clearAttachments) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        notice.update(requestDto.getTitle(), requestDto.getText());

        // 전체 삭제 후 대체
        if (clearImages) {
            mediaAttachmentService.removeAll(MediaType.NOTICE, noticeId, me);
        }
        if (clearAttachments) {
            mediaAttachmentService.removeAll(MediaType.NOTICE_ATTACHMENT, noticeId, me);
        }

        // 새 파일 업로드(추가 또는 대체 후 재등록)
        if (images != null && !images.isEmpty()) {
            mediaAttachmentService.addByMultipart(MediaType.NOTICE, noticeId, me, images, imageDir);
        }
        if (attachments != null && !attachments.isEmpty()) {
            mediaAttachmentService.addByMultipart(MediaType.NOTICE_ATTACHMENT, noticeId, me, attachments, attachDir);
        }

        // 공지 수정 알림 (간단: 수정도 새 공지와 동일하게 처리할지 여부는 정책에 따라) 여기서는 생략 가능
        return NoticeResponse.of(notice);
    }

    /**
     * 공지 삭제(첨부 포함)
     * <P>
     * 공지와 연결된 모든 이미지/첨부를 정리 후 엔티티를 삭제합니다.
     * </P>
     * 
     * @PARAM noticeId 공지 ID
     * @PARAM me 삭제자(관리자) Member
     * @RETURN 없음
     */
    public void delete(Long noticeId, Member me) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        // 첨부 정리
        mediaAttachmentService.removeAll(MediaType.NOTICE, noticeId, me);
        mediaAttachmentService.removeAll(MediaType.NOTICE_ATTACHMENT, noticeId, me);

        // 엔티티 삭제
        noticeRepository.delete(notice);
    }

    // ===== 내부 유틸 =====
    private void broadcastNoticeAsync(Notice notice, Member author) {
        try {
            // 모든 활성 회원 ID (간단 구현: 전체) - 규모 커지면 paging 필요
            var allIds = memberRepository.findAll().stream().map(Member::getId).toList();
            for (Long mid : allIds) {
                if (author != null && author.getId().equals(mid))
                    continue; // 작성자 제외
                // NotificationTypeCode 에 NOTICE 관련 항목이 없다면 WISH_AUCTION_STARTED 와 같은 기존 코드 재사용은
                // 의미상 부적절
                // => ENUM 확장이 필요. 임시로 AUCTION_CANCELED 재사용하지 않고 추가 정의 권장.
                // 안전하게 존재하는 GENERIC 용이 없어 아무 것도 보내지 않으려면 조기 return.
                // 여기서는 임시로 AUCTION_CANCELED 를 placeholder 로 사용하고, 프론트에서 type=AUCTION 으로 그룹화됨을
                // 감안.
                notificationCommandService.create(mid, NotificationTypeCode.NOTICE_NEW, notice.getId(),
                        notice.getTitle(), truncateBody(notice));
            }
        } catch (Exception e) {
            // 브로드캐스트 실패는 롤백 유발하지 않고 로깅만
            // (별도 비동기 큐/배치로 전환 가능)
        }
    }

    private String truncateBody(Notice notice) {
        String text = notice.getText();
        if (text == null)
            return "";
        String plain = text.replaceAll("\n+", " ").trim();
        return plain.length() > 80 ? plain.substring(0, 80) + "…" : plain;
    }
}
