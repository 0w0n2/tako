package com.bukadong.tcg.api.media.guard;

import com.bukadong.tcg.api.media.config.UploadProperties;
import com.bukadong.tcg.api.media.config.UploadProperties.Image;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
@RequiredArgsConstructor
public class MediaUploadGuard {

    private static final Logger log = LoggerFactory.getLogger(MediaUploadGuard.class);

    private final UploadProperties uploadProperties;

    /**
     * 멀티파트 업로드 전체 검증을 수행합니다.
     * <p>
     * 다음 항목을 순차적으로 검사합니다.
     * <ul>
     * <li>요청 파일 존재 여부</li>
     * <li>파일 개수 제한</li>
     * <li>요청 전체 바이트 합 제한</li>
     * <li>각 파일의 크기, MIME, 확장자, 이미지 규격(선택) 검증</li>
     * </ul>
     * 검증 실패 시 {@link BaseException} 을 발생시킵니다.
     *
     * @param type  업로드 미디어 타입
     * @param files 업로드된 파일 목록
     * @throws NullPointerException type 가 null 인 경우
     * @throws BaseException        정책 위반(BAD_REQUEST, PAYLOAD_TOO_LARGE,
     *                              MEDIA_UNSUPPORTED_TYPE 등)
     */
    public void validate(MediaType type, List<MultipartFile> files) {
        Objects.requireNonNull(type, "type must not be null");
        List<MultipartFile> validated = validateFilesPresent(type, files);

        UploadProperties.Limit rule = Objects.requireNonNull(resolveRule(type), "upload rule must not be null");
        if (log.isDebugEnabled()) {
            log.debug("[UploadGuard] Start validate. type={} files={} rule={}", type, validated.size(),
                    ruleToString(rule));
        }

        validateCountLimit(type, validated.size(), rule);
        validateTotalSize(type, validated, rule);
        validateEachFile(type, validated, rule);

        if (log.isDebugEnabled()) {
            log.debug("[UploadGuard] Validate success. type={} files={}", type, validated.size());
        }
    }

    /**
     * 파일 목록이 비어있지 않은지 검증합니다.
     *
     * @param type  업로드 미디어 타입(로그용)
     * @param files 업로드된 파일 목록
     * @return 원본 파일 목록(비어있지 않은 경우)
     * @throws BaseException 파일 목록이 null 또는 비어있는 경우(BAD_REQUEST)
     */
    private List<MultipartFile> validateFilesPresent(MediaType type, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            log.warn("[UploadGuard] Empty files. type={}", type);
            throw new BaseException(BaseResponseStatus.BAD_REQUEST);
        }
        return files;
    }

    /**
     * 업로드 파일 개수 제한을 검증합니다.
     *
     * @param type  업로드 미디어 타입(로그용)
     * @param count 업로드된 파일 개수
     * @param rule  업로드 제한 규칙
     * @throws BaseException 개수 초과 시(PAYLOAD_TOO_LARGE)
     */
    private void validateCountLimit(MediaType type, int count, UploadProperties.Limit rule) {
        if (isPositive(rule.getMaxCount()) && count > rule.getMaxCount()) {
            log.warn("[UploadGuard] Max count exceeded. type={} max={} actual={}", type, rule.getMaxCount(), count);
            throw new BaseException(BaseResponseStatus.PAYLOAD_TOO_LARGE);
        }
    }

    /**
     * 요청 전체 파일 크기(합계)가 제한 내인지 검증합니다.
     * 제한값이 0 또는 null이면 검증을 생략합니다.
     *
     * @param type  업로드 미디어 타입(로그용)
     * @param files 업로드된 파일 목록
     * @param rule  업로드 제한 규칙
     * @throws BaseException 총합 초과 시(PAYLOAD_TOO_LARGE)
     */
    private void validateTotalSize(MediaType type, List<MultipartFile> files, UploadProperties.Limit rule) {
        if (!nonZero(rule.getPerRequest()))
            return;
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

    /**
     * 각 파일에 대해 단일 파일 검증을 수행합니다.
     *
     * @param type  업로드 미디어 타입(로그용)
     * @param files 업로드된 파일 목록
     * @param rule  업로드 제한 규칙
     */
    private void validateEachFile(MediaType type, List<MultipartFile> files, UploadProperties.Limit rule) {
        for (int i = 0; i < files.size(); i++) {
            MultipartFile f = files.get(i);
            validateSingleFile(type, rule, f, i);
        }
    }

    /**
     * 단일 파일에 대한 상세 검증을 수행합니다.
     * per-file 크기, MIME, 확장자, 이미지 규격(선택)을 확인합니다.
     *
     * @param type  업로드 미디어 타입(로그용)
     * @param rule  업로드 제한 규칙
     * @param f     검증 대상 파일
     * @param index 파일 인덱스(로그용)
     * @throws BaseException 정책 위반 시 적절한 상태 코드로 예외 발생
     */
    private void validateSingleFile(MediaType type, UploadProperties.Limit rule, MultipartFile f, int index) {
        if (f == null || f.isEmpty()) {
            log.warn("[UploadGuard] Empty file item. type={} index={}", type, index);
            throw new BaseException(BaseResponseStatus.BAD_REQUEST);
        }
        String name = f.getOriginalFilename();
        long size = f.getSize();
        String mime = safeLower(f.getContentType());

        if (log.isDebugEnabled()) {
            log.debug("[UploadGuard] File[{}] name='{}' size={}B mime={}", index, name, size, mime);
        }

        // per-file 크기
        if (nonZero(rule.getPerFile()) && size > rule.getPerFile().toBytes()) {
            log.warn("[UploadGuard] Per-file size exceeded. type={} index={} size={}B limit={}B name='{}'", type, index,
                    size, rule.getPerFile().toBytes(), name);
            throw new BaseException(BaseResponseStatus.PAYLOAD_TOO_LARGE);
        }

        // MIME
        if (!isMimeAllowed(mime, rule.getAllowedMimes())) {
            log.warn("[UploadGuard] MIME not allowed. type={} index={} mime={} allowed={}", type, index, mime,
                    rule.getAllowedMimes());
            throw new BaseException(BaseResponseStatus.MEDIA_UNSUPPORTED_TYPE);
        }

        // 확장자
        if (!isExtAllowed(name, rule.getAllowedExts())) {
            log.warn("[UploadGuard] Extension not allowed. type={} index={} name='{}' allowedExts={}", type, index,
                    name, rule.getAllowedExts());
            throw new BaseException(BaseResponseStatus.MEDIA_UNSUPPORTED_TYPE);
        }

        // 이미지 규격
        if (isImageMime(mime) && rule.getImage() != null) {
            if (log.isDebugEnabled()) {
                log.debug("[UploadGuard] Validate image rules. type={} index={} name='{}' imageRule={}", type, index,
                        name, imageRuleToString(rule.getImage()));
            }
            validateImageRule(f, rule.getImage(), type, index);
        }
    }

    /**
     * 주어진 MIME이 이미지 타입인지 여부를 반환합니다.
     *
     * @param mime MIME 문자열
     * @return 이미지 타입이면 true, 아니면 false
     */
    private boolean isImageMime(String mime) {
        return mime != null && mime.startsWith("image/");
    }

    // ----- helpers -----

    /**
     * 업로드 타입에 해당하는 제한 규칙을 조회합니다.
     * 설정에 없을 경우 합리적인 기본값을 반환합니다.
     *
     * @param type 업로드 미디어 타입
     * @return 제한 규칙 객체
     */
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

    /**
     * 주어진 MIME이 허용 리스트/패턴에 부합하는지 검사합니다.
     *
     * 패턴 규칙:
     * <ul>
     * <li>"image/*": 접두 일치("image/")</li>
     * <li>"image/": 접두 일치</li>
     * <li>"image/png": 정확 일치</li>
     * </ul>
     *
     * @param mime    검사할 MIME 문자열
     * @param allowed 허용 MIME 패턴 목록(null 또는 비어있으면 모두 허용)
     * @return 허용되면 true, 아니면 false
     */
    private boolean isMimeAllowed(String mime, List<String> allowed) {
        if (!StringUtils.hasText(mime))
            return false;
        if (allowed == null || allowed.isEmpty())
            return true;

        String m = mime.toLowerCase(Locale.ROOT).trim();
        for (String pat : allowed) {
            if (!StringUtils.hasText(pat))
                continue;
            String p = pat.toLowerCase(Locale.ROOT).trim();
            if (matchesMime(m, p))
                return true;
        }
        return false;
    }

    /**
     * 단일 MIME 패턴과 실제 MIME의 매칭 여부를 반환합니다.
     *
     * @param mime    실제 MIME (소문자/trim 처리된 값 기대)
     * @param pattern 패턴 (소문자/trim 처리된 값 기대)
     * @return 매칭되면 true, 아니면 false
     */
    private boolean matchesMime(String mime, String pattern) {
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 1); // "image/*" -> "image/"
            return mime.startsWith(prefix);
        }
        if (pattern.endsWith("/")) {
            return mime.startsWith(pattern); // "image/"
        }
        return mime.equals(pattern); // exact like "image/png"
    }

    /**
     * 파일 확장자가 허용 리스트에 포함되는지 검사합니다.
     * 허용 리스트가 비어있거나 null이면 모든 확장자를 허용합니다.
     *
     * @param filenameOrExt 파일명 또는 확장자 문자열
     * @param allowed       허용 확장자 목록
     * @return 허용되면 true, 아니면 false
     */
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

    /**
     * 파일명이나 확장자 문자열에서 확장자 부분만 추출합니다.
     *
     * 규칙:
     * <ul>
     * <li>경로 구분자('/')가 있으면 마지막 조각을 사용</li>
     * <li>마지막 점('.') 이후의 문자열을 확장자로 간주</li>
     * <li>점이 없으면 전체를 확장자로 간주</li>
     * </ul>
     *
     * @param filenameOrExt 파일명 또는 확장자
     * @return 추출된 확장자, 유효하지 않으면 null
     */
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

    /**
     * 이미지 규격(정사각형, 최소/최대 너비·높이)을 검증합니다.
     * 파일이 이미지가 아니거나 읽기에 실패한 경우 미디어 타입 미지원으로 처리합니다.
     *
     * @param file  업로드 파일
     * @param rule  이미지 규칙(선택)
     * @param type  업로드 미디어 타입(로그용)
     * @param index 파일 인덱스(로그용)
     * @throws BaseException 규칙 위반 또는 이미지 판독 실패 시
     */
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

    /**
     * 양의 정수인지 여부를 반환합니다.
     *
     * @param n 정수
     * @return n > 0 이면 true
     */
    private boolean isPositive(Integer n) {
        return n != null && n > 0;
    }

    /**
     * 데이터 크기가 0보다 큰지 여부를 반환합니다.
     *
     * @param size 데이터 크기
     * @return 바이트 값이 0보다 크면 true
     */
    private boolean nonZero(DataSize size) {
        return size != null && size.toBytes() > 0;
    }

    /**
     * null-safe 소문자 변환을 수행합니다.
     *
     * @param s 입력 문자열
     * @return 소문자로 변환된 문자열 또는 null
     */
    private String safeLower(String s) {
        return (s == null) ? null : s.toLowerCase(Locale.ROOT);
    }

    /**
     * 업로드 제한 규칙을 로깅용 문자열로 변환합니다.
     *
     * @param r 제한 규칙
     * @return 가독성 있는 문자열 표현
     */
    private String ruleToString(UploadProperties.Limit r) {
        if (r == null)
            return "null";
        return String.format("{perFile=%sB, perRequest=%sB, maxCount=%s, allowedMimes=%s, allowedExts=%s, image=%s}",
                (r.getPerFile() == null ? null : r.getPerFile().toBytes()),
                (r.getPerRequest() == null ? null : r.getPerRequest().toBytes()), r.getMaxCount(), r.getAllowedMimes(),
                r.getAllowedExts(), imageRuleToString(r.getImage()));
    }

    /**
     * 이미지 규칙을 로깅용 문자열로 변환합니다.
     *
     * @param i 이미지 규칙
     * @return 가독성 있는 문자열 표현
     */
    private String imageRuleToString(Image i) {
        if (i == null)
            return "null";
        return String.format("{square=%s, minW=%s, minH=%s, maxW=%s, maxH=%s}", i.getSquare(), i.getMinWidth(),
                i.getMinHeight(), i.getMaxWidth(), i.getMaxHeight());
    }
}
