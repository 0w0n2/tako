package com.bukadong.tcg.common.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 생성일, 수정일 관리용 베이스 엔티티
 * createdAt, updatedAt 필드를 자동으로 관리.
 * jpa entity 클래스에 extends 해서 사용하면 됩니둥.
 */
@MappedSuperclass
@Getter
public class BaseEntity {

    @Column(updatable = false)
    private LocalDateTime createdAt; // 최초 생성일

    private LocalDateTime updatedAt; // 마지막 수정일

    @PrePersist // 저장 전에 동작
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    @PreUpdate // 업데이트 전에 동작
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
