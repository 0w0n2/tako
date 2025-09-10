package com.bukadong.tcg.api.member.repository;

import com.bukadong.tcg.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import javax.swing.text.html.Option;
import java.util.Optional;

/**
 * 회원 엔티티({@link Member})용 Spring Data JPA 레포지토리.
 *
 * <p>
 * 기본적인 CRUD 연산은 {@link JpaRepository}를 통해 제공되며,
 * 이메일 및 닉네임 중복 여부를 확인하는 메서드를 추가로 제공한다.
 * </p>
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 이메일 중복 여부 확인 (대소문자 구분).
     *
     * @param email 검증할 이메일
     * @return 이미 존재하면 true, 없으면 false
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 중복 여부 확인 (대소문자 구분).
     *
     * @param nickname 검증할 닉네임
     * @return 이미 존재하면 true, 없으면 false
     */
    boolean existsByNickname(String nickname);

    Optional<Member> findByEmail(String email);
}
