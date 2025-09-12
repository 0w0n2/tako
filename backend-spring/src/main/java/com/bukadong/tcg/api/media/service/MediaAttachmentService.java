package com.bukadong.tcg.api.media.service;

import com.bukadong.tcg.api.media.dto.response.MediaUploadResponse;
import com.bukadong.tcg.api.media.entity.Media;
import com.bukadong.tcg.api.media.entity.MediaKind;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.policy.MediaPermissionRegistry;
import com.bukadong.tcg.api.media.repository.MediaRepository;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.dto.S3UploadResult;
import com.bukadong.tcg.global.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 범용 미디어 첨부 서비스 (도메인 독립)
 * <P>
 * MediaType/ownerId/actor만 주면 첨부 추가/삭제 처리.
 * </P>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MediaAttachmentService {

    private final MediaPermissionRegistry permissionRegistry;
    private final MediaRepository mediaRepository;
    private final S3Uploader s3Uploader;

    /**
     * (멀티파트) 파일 업로드 + 첨부 추가
     * 
     * @param type
     * @param ownerId
     * @param actor
     * @param files
     * @param dir
     * @return
     */
    public MediaUploadResponse addByMultipart(MediaType type, Long ownerId, Member actor, List<MultipartFile> files,
            String dir) {
        permissionRegistry.get(type).checkCanAdd(type, ownerId, actor);

        List<Media> exist = mediaRepository.findByTypeAndOwnerIdOrderBySeqNoAsc(type, ownerId);
        int seq = exist.size() + 1;

        List<MediaUploadResponse.Item> items = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty())
                continue;

            S3UploadResult res = s3Uploader.upload(f, dir);
            Media m = Media.builder().type(type).ownerId(ownerId).key(res.getKey()) // KEY 저장
                    .mediaKind(MediaKind.IMAGE) // 이미지 기본, 필요 시 요청 파라미터로 받기
                    .mimeType(res.getContentType()).seqNo(seq++).build();
            mediaRepository.save(m);

            items.add(MediaUploadResponse.Item.builder().key(res.getKey()).originalFilename(res.getOriginalFilename())
                    .contentType(res.getContentType()).size(res.getSize()).build());
        }
        return MediaUploadResponse.builder().items(items).build();
    }

    /**
     * (사전 업로드된) key/url 목록으로 첨부 추가
     * 
     * @param type
     * @param ownerId
     * @param actor
     * @param keysOrUrls
     * @param mimeType
     * @param kind
     */
    public void addByKeys(MediaType type, Long ownerId, Member actor, List<String> keysOrUrls, String mimeType,
            MediaKind kind) {
        permissionRegistry.get(type).checkCanAdd(type, ownerId, actor);

        List<Media> exist = mediaRepository.findByTypeAndOwnerIdOrderBySeqNoAsc(type, ownerId);
        int seq = exist.size() + 1;

        if (keysOrUrls == null)
            return;
        for (String keyOrUrl : keysOrUrls) {
            if (keyOrUrl == null || keyOrUrl.isBlank())
                continue;
            Media m = Media.builder().type(type).ownerId(ownerId).key(keyOrUrl) // key 저장
                    .mediaKind(kind == null ? MediaKind.IMAGE : kind).mimeType(mimeType).seqNo(seq++).build();
            mediaRepository.save(m);
        }
    }

    /**
     * 첨부 삭제 + S3 오브젝트 삭제 시도
     * 
     * @param type
     * @param ownerId
     * @param mediaId
     * @param actor
     */
    public void remove(MediaType type, Long ownerId, Long mediaId, Member actor) {
        permissionRegistry.get(type).checkCanDelete(type, ownerId, mediaId, actor);

        Media media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null)
            return;
        if (media.getType() != type || !media.getOwnerId().equals(ownerId))
            return;

        tryDeleteS3Object(media.getKey());
        mediaRepository.delete(media);

        resequence(type, ownerId);
    }

    /**
     * 첨부 삭제 후 seq_no 재정렬
     * 
     * @param type
     * @param ownerId
     */
    private void resequence(MediaType type, Long ownerId) {
        List<Media> rest = mediaRepository.findByTypeAndOwnerIdOrderBySeqNoAsc(type, ownerId);
        int seq = 1;
        for (Media m : rest) {
            if (m.getSeqNo() != seq)
                m.setSeqNo(seq);
            seq++;
        }
    }

    private void tryDeleteS3Object(String urlOrKey) {
        if (urlOrKey == null || urlOrKey.isBlank())
            return;
        String lower = urlOrKey.toLowerCase();
        String fileKey = urlOrKey;
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            try {
                URI uri = URI.create(urlOrKey);
                String path = uri.getPath();
                if (path != null && !path.isBlank()) {
                    fileKey = path.startsWith("/") ? path.substring(1) : path;
                    int firstSlash = fileKey.indexOf('/');
                    if (firstSlash > 0)
                        fileKey = fileKey.substring(firstSlash + 1);
                }
            } catch (Exception ignored) {
                return;
            }
        }
        if (fileKey != null && !fileKey.isBlank() && !fileKey.startsWith("http")) {
            try {
                s3Uploader.delete(fileKey);
            } catch (Exception ignored) {

            }
        }
    }

    /**
     * S3 연결 헬스체크
     * <P>
     * 버킷에서 객체 리스트를 조회하여 연결 여부를 확인
     * </P>
     * 
     * @RETURN true=정상 연결, false=실패
     */
    @Transactional(readOnly = true)
    public boolean healthCheck() {
        return s3Uploader.healthCheck();
    }
}
