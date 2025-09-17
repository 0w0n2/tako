package com.bukadong.tcg.api.admin.notice.service;

import com.bukadong.tcg.api.admin.notice.dto.request.NoticeCreateRequest;
import com.bukadong.tcg.api.admin.notice.dto.request.NoticeUpdateRequest;
import com.bukadong.tcg.api.admin.notice.dto.response.NoticeResponse;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaAttachmentService;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.notice.entity.Notice;
import com.bukadong.tcg.api.notice.repository.NoticeRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import jakarta.persistence.EntityManager;
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
@RequiredArgsConstructor
public class AdminNoticeCommandService {

    private final NoticeRepository noticeRepository;
    private final EntityManager em;
    private final MediaAttachmentService mediaAttachmentService;

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
    @Transactional
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
    @Transactional
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
    @Transactional
    public void delete(Long noticeId, Member me) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        // 첨부 정리
        mediaAttachmentService.removeAll(MediaType.NOTICE, noticeId, me);
        mediaAttachmentService.removeAll(MediaType.NOTICE_ATTACHMENT, noticeId, me);

        // 엔티티 삭제
        noticeRepository.delete(notice);
    }
}
