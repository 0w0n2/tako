package com.bukadong.tcg.api.card.entity;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseEntity;
import com.bukadong.tcg.global.util.BigIntegerToStringConverter;
import jakarta.persistence.*;
import jnr.a64asm.Mem;
import lombok.*;
import org.springframework.util.StringUtils;
import org.web3j.crypto.Hash;

import java.math.BigInteger;
import java.util.Optional;

/**
 * 실물 카드 엔티티
 *
 * <p>
 * 회원이 보유한 실물 카드를 관리한다.
 * </p>
 *
 * <ul>
 * <li>card_id: 카드 마스터와 연관</li>
 * <li>owner_member_id: 소유 회원</li>
 * <li>uuid: 실물 카드 식별용, 고유 제약</li>
 * </ul>
 */
@Entity
@Table(name = "physical_card", uniqueConstraints = {@UniqueConstraint(name = "uk_pcard_token_id", columnNames = "token_id"), @UniqueConstraint(name = "uk_pcard_secret_hash", columnNames = "secret_hash")}, indexes = {@Index(name = "idx_pcard_card", columnList = "card_id"), @Index(name = "idx_pcard_owner", columnList = "owner_member_id"), @Index(name = "idx_pcard_token_id", columnList = "token_id")})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalCard extends BaseEntity {

    /* 실물 카드 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* 카드 마스터 정보 (어떤 종류의 카드인지) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false, foreignKey = @ForeignKey(name = "FK_physical_card_card"))
    private Card card;

    /* 소유 회원 (클레임 전에는 NULL일 수 있음) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_member_id", foreignKey = @ForeignKey(name = "FK_physical_card_owner"))
    private Member owner;

    /* 비회원 소유자의 지갑 주소 */
    @Column(name = "owner_wallet_address", length = 50)
    private String ownerWalletAddress;

    /* 블록체인 상의 고유 NFT ID (uint256) */
    @Convert(converter = BigIntegerToStringConverter.class)
    @Column(name = "token_id", unique = true, length = 80)
    private BigInteger tokenId;

    /* 클레임용 시크릿 코드의 해시값 */
    @Column(name = "secret_hash", unique = true)
    private String secretHash;

    /* NFT 상태 (발행 대기, 발행 완료, 클레임 완료) */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhysicalCardStatus status = PhysicalCardStatus.PENDING;

    public void markAsMinted(String secret) {
        this.status = PhysicalCardStatus.MINTED;
        if (StringUtils.hasText(secret)) {
            this.secretHash = Hash.sha3String(secret);
        }
    }

    public void markAsFailed() {
        this.status = PhysicalCardStatus.FAILED;
    }

    public void markAsClaimed(String walletAddress, Member owner) {
        this.status = PhysicalCardStatus.CLAIMED;
        if (StringUtils.hasText(walletAddress)) {
            this.ownerWalletAddress = walletAddress;
        }
        if (owner != null) {
            this.owner = owner;
        }
    }
}
