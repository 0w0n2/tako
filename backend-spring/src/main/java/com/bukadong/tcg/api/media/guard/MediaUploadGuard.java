package com.bukadong.tcg.api.media.guard;

import com.bukadong.tcg.api.media.config.UploadProperties;
import com.bukadong.tcg.api.media.config.UploadProperties.Image;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaUploadGuard {

    private final UploadProperties uploadProperties;

    /** 멀티파트 업로드 전체 검증 */
    public void validate(MediaType type, List<MultipartFile> files) {
        Objects.requireNonNull(type, "type must not be null");
        if (files == null || files.isEmpty()) {
            log.warn("[UploadGuard] Empty files. type={}", type);
            throw new BaseException(BaseResponseStatus.BAD_REQUEST);
        }

        UploadProperties.Limit rule = resolveRule(type);
        if (log.isDebugEnabled()) {
            log.debug("[UploadGuard] Start validate. type={} files={} rule={}", type, files.size(), ruleToString(rule));
        }

        // 개수 제한
        if (isPositive(rule.getMaxCount()) && files.size() > rule.getMaxCount()) {
            log.warn("[UploadGuard] Max count exceeded. type={} max={} actual={}", type, rule.getMaxCount(),
                    files.size());
            throw new BaseException(BaseResponseStatus.PAYLOAD_TOO_LARGE);
        }

        // 요청 총합
        if (nonZero(rule.getPerRequest())) {
            long total = files.stream().filter(Objects::nonNull).mapToLong(MultipartFile::getSize).sum();
            if (log.isDebugEnabled()) {
                log.debug("[UploadGuard] Total size check. type={} total={}B limit={}B", type, total,
                        rule.getPerRequest().toBytes());
            }
            if (total > rule.getPerRequest().toBytes()) {
                log.warn("[UploadGuard] Per-request size exceeded. type={} total={}B limit={}B", type, total,
                        rule.getPerRequest().toBytes());
                throw new BaseException(BaseResponseStatus.PAYLOAD_TOO_LARGE);
            }
        }

        // 파일 개별 검증
        for (int i = 0; i < files.size(); i++) {
            MultipartFile f = files.get(i);
            if (f == null || f.isEmpty()) {
                log.warn("[UploadGuard] Empty file item. type={} index={}", type, i);
                throw new BaseException(BaseResponseStatus.BAD_REQUEST);
            }

            String name = f.getOriginalFilename();
            long size = f.getSize();
            String mime = safeLower(f.getContentType());

            if (log.isDebugEnabled()) {
                log.debug("[UploadGuard] File[{}] name='{}' size={}B mime={}", i, name, size, mime);
            }

            // per-file 크기
            if (nonZero(rule.getPerFile()) && size > rule.getPerFile().toBytes()) {
                log.warn("[UploadGuard] Per-file size exceeded. type={} index={} size={}B limit={}B name='{}'", type, i,
                        size, rule.getPerFile().toBytes(), name);
                throw new BaseException(BaseResponseStatus.PAYLOAD_TOO_LARGE);
            }

            // MIME
            if (!isMimeAllowed(mime, rule.getAllowedMimes())) {
                log.warn("[UploadGuard] MIME not allowed. type={} index={} mime={} allowed={}", type, i, mime,
                        rule.getAllowedMimes());
                throw new BaseException(BaseResponseStatus.MEDIA_UNSUPPORTED_TYPE);
            }

            // 확장자
            if (!isExtAllowed(name, rule.getAllowedExts())) {
                log.warn("[UploadGuard] Extension not allowed. type={} index={} name='{}' allowedExts={}", type, i,
                        name, rule.getAllowedExts());
                throw new BaseException(BaseResponseStatus.MEDIA_UNSUPPORTED_TYPE);
            }

            // 이미지 규격
            if (mime != null && mime.startsWith("image/") && rule.getImage() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("[UploadGuard] Validate image rules. type={} index={} name='{}' imageRule={}", type, i,
                            name, imageRuleToString(rule.getImage()));
                }
                validateImageRule(f, rule.getImage(), type, i);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("[UploadGuard] Validate success. type={} files={}", type, files.size());
        }
    }

    // ----- helpers -----

    private UploadProperties.Limit resolveRule(MediaType type) {
        Map<MediaType, UploadProperties.Limit> map = uploadProperties.getLimits();
        if (map != null && map.containsKey(type)) {
            return map.get(type);
        }
        // 기본값 (설정 없을 때)
        UploadProperties.Limit def = new UploadProperties.Limit();
        def.setPerFile(DataSize.ofMegabytes(10));
        def.setPerRequest(DataSize.ofMegabytes(50));
        def.setMaxCount(10);
        def.setAllowedMimes(List.of("image/")); // 접두 허용
        def.setAllowedExts(List.of("jpg", "jpeg", "png", "webp"));
        return def;
    }

    private boolean isMimeAllowed(String mime, List<String> allowed) {
        if (!StringUtils.hasText(mime))
            return false;
        if (allowed == null || allowed.isEmpty())
            return true;

        for (String pat : allowed) {
            if (!StringUtils.hasText(pat))
                continue;
            String p = pat.toLowerCase(Locale.ROOT).trim();
            if (p.endsWith("/*")) {
                String prefix = p.substring(0, p.length() - 1); // "image/*" -> "image/"
                if (mime.startsWith(prefix))
                    return true;
            } else if (p.endsWith("/")) {
                if (mime.startsWith(p))
                    return true; // "image/"
            } else {
                if (mime.equals(p))
                    return true; // "image/png"
            }
        }
        return false;
    }

    private boolean isExtAllowed(String filenameOrExt, List<String> allowed) {
        if (allowed == null || allowed.isEmpty())
            return true;
        String ext = extractExt(filenameOrExt);
        if (!StringUtils.hasText(ext))
            return false;
        String e = ext.toLowerCase(Locale.ROOT);
        for (String a : allowed) {
            if (e.equals(a.toLowerCase(Locale.ROOT)))
                return true;
        }
        return false;
    }

    private String extractExt(String filenameOrExt) {
        if (!StringUtils.hasText(filenameOrExt))
            return null;
        String s = filenameOrExt.trim();
        int slash = s.lastIndexOf('/');
        if (slash >= 0)
            s = s.substring(slash + 1);
        int dot = s.lastIndexOf('.');
        return (dot >= 0 && dot + 1 < s.length()) ? s.substring(dot + 1) : s;
    }

    private void validateImageRule(MultipartFile file, Image rule, MediaType type, int index) {
        try (InputStream in = file.getInputStream()) {
            BufferedImage bi = ImageIO.read(in);
            if (bi == null) {
                log.warn("[UploadGuard] Not an image or unreadable. type={} index={} name='{}'", type, index,
                        file.getOriginalFilename());
                throw new BaseException(BaseResponseStatus.MEDIA_UNSUPPORTED_TYPE);
            }

            int w = bi.getWidth();
            int h = bi.getHeight();

            if (log.isDebugEnabled()) {
                log.debug("[UploadGuard] Image dims. type={} index={} name='{}' width={} height={}", type, index,
                        file.getOriginalFilename(), w, h);
            }

            if (Boolean.TRUE.equals(rule.getSquare()) && w != h) {
                log.warn("[UploadGuard] Image must be square. type={} index={} name='{}' width={} height={}", type,
                        index, file.getOriginalFilename(), w, h);
                throw new BaseException(BaseResponseStatus.MEDIA_FILE_RULE_VIOLATION);
            }
            if (rule.getMinWidth() != null && w < rule.getMinWidth()) {
                log.warn("[UploadGuard] Image width too small. type={} index={} name='{}' width={} min={}", type, index,
                        file.getOriginalFilename(), w, rule.getMinWidth());
                throw new BaseException(BaseResponseStatus.MEDIA_FILE_RULE_VIOLATION);
            }
            if (rule.getMinHeight() != null && h < rule.getMinHeight()) {
                log.warn("[UploadGuard] Image height too small. type={} index={} name='{}' height={} min={}", type,
                        index, file.getOriginalFilename(), h, rule.getMinHeight());
                throw new BaseException(BaseResponseStatus.MEDIA_FILE_RULE_VIOLATION);
            }
            if (rule.getMaxWidth() != null && w > rule.getMaxWidth()) {
                log.warn("[UploadGuard] Image width too large. type={} index={} name='{}' width={} max={}", type, index,
                        file.getOriginalFilename(), w, rule.getMaxWidth());
                throw new BaseException(BaseResponseStatus.MEDIA_FILE_RULE_VIOLATION);
            }
            if (rule.getMaxHeight() != null && h > rule.getMaxHeight()) {
                log.warn("[UploadGuard] Image height too large. type={} index={} name='{}' height={} max={}", type,
                        index, file.getOriginalFilename(), h, rule.getMaxHeight());
                throw new BaseException(BaseResponseStatus.MEDIA_FILE_RULE_VIOLATION);
            }
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[UploadGuard] Image validation error. type={} index={} name='{}' err={}", type, index,
                    file.getOriginalFilename(), e.toString());
            throw new BaseException(BaseResponseStatus.MEDIA_UNSUPPORTED_TYPE);
        }
    }

    private boolean isPositive(Integer n) {
        return n != null && n > 0;
    }

    private boolean nonZero(DataSize size) {
        return size != null && size.toBytes() > 0;
    }

    private String safeLower(String s) {
        return (s == null) ? null : s.toLowerCase(Locale.ROOT);
    }

    private String ruleToString(UploadProperties.Limit r) {
        if (r == null)
            return "null";
        return String.format("{perFile=%sB, perRequest=%sB, maxCount=%s, allowedMimes=%s, allowedExts=%s, image=%s}",
                (r.getPerFile() == null ? null : r.getPerFile().toBytes()),
                (r.getPerRequest() == null ? null : r.getPerRequest().toBytes()), r.getMaxCount(), r.getAllowedMimes(),
                r.getAllowedExts(), imageRuleToString(r.getImage()));
    }

    private String imageRuleToString(Image i) {
        if (i == null)
            return "null";
        return String.format("{square=%s, minW=%s, minH=%s, maxW=%s, maxH=%s}", i.getSquare(), i.getMinWidth(),
                i.getMinHeight(), i.getMaxWidth(), i.getMaxHeight());
    }
}
