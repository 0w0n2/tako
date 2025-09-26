package com.bukadong.tcg.api.member.trust.entity;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_trust", indexes = { @Index(name = "idx_member_trust_member", columnList = "member_id") })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MemberTrust extends BaseEntity {

    public static final int DEFAULT_SCORE = 365;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Column(name = "score", nullable = false)
    private int score;

    public void applyDelta(int delta) {
        this.score += delta;
        if (this.score < 0)
            this.score = 0; // 하한 보호 (0점 미만 불가)
    }
}
