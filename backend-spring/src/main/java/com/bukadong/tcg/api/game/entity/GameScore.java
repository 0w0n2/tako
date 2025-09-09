package com.bukadong.tcg.api.game.entity;

import com.bukadong.tcg.global.common.base.BaseEntity;
import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 미니게임 점수 엔티티
 *
 * <p>
 * 회원별 미니게임 점수를 기록한다.
 * 현재는 RPS(가위바위보)만 지원되지만,
 * 추후 다양한 게임 타입으로 확장 가능하다.
 * </p>
 */
@Entity
@Table(name = "game_score", indexes = {
        @Index(name = "idx_game_score_member_id", columnList = "member_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameScore extends BaseEntity {

    /** 점수 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 회원 (FK) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_game_score_member"))
    private Member member;

    /** 점수 (BIGINT) */
    @Column(name = "score", nullable = false)
    private Long score;

    /** 게임 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private GameType type;
}
