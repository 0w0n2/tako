package com.bukadong.tcg.notice.repository;

import com.bukadong.tcg.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 공지사항 엔티티({@link Notice})용 Spring Data JPA 레포지토리.
 *
 * <p>
 * 기본 CRUD 연산 및 페이징/정렬 기능을 {@link JpaRepository}를 통해 제공한다.
 * </p>
 *
 * <ul>
 * <li>{@code save()} - 공지사항 생성/수정</li>
 * <li>{@code findById()} - 공지사항 단건 조회</li>
 * <li>{@code findAll()} - 전체 목록 조회 (페이징 지원)</li>
 * <li>{@code deleteById()} - 공지사항 삭제</li>
 * </ul>
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
