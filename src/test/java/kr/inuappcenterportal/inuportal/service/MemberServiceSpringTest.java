package kr.inuappcenterportal.inuportal.service;


import kr.inuappcenterportal.inuportal.domain.member.dto.LoginDto;
import kr.inuappcenterportal.inuportal.domain.member.dto.MemberResponseDto;
import kr.inuappcenterportal.inuportal.domain.member.dto.MemberUpdateNicknameDto;
import kr.inuappcenterportal.inuportal.domain.member.dto.TokenDto;
import kr.inuappcenterportal.inuportal.domain.member.fixture.MemberFixture;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.repository.MemberRepository;
import kr.inuappcenterportal.inuportal.domain.member.service.MemberService;
import kr.inuappcenterportal.inuportal.global.config.TokenProvider;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import kr.inuappcenterportal.inuportal.global.service.RedisService;
import module.IntegrationSupportTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;



class MemberServiceSpringTest extends IntegrationSupportTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    RedisService redisService;
    @Autowired
    TokenProvider tokenProvider;

    @Test
    void 첫_로그인시_회원가입_후_로그인을_진행한다() {
        //given
        LoginDto loginDto = MemberFixture.MEMBER_FIXTURE_1.login();

        //when
        TokenDto tokenDto = memberService.schoolLogin(loginDto);

        // then
        assertAll(
                () -> assertThat(tokenDto.getAccessToken()).isNotNull(),
                () -> assertThat(tokenDto.getRefreshToken()).isNotNull()
        );
    }

    @Test
    void 로그인을_성공한다() {
        //given
        memberRepository.save(MemberFixture.MEMBER_FIXTURE_1.create());
        LoginDto loginDto = MemberFixture.MEMBER_FIXTURE_1.login();

        //when
        TokenDto tokenDto = memberService.schoolLogin(loginDto);

        //then
        assertAll(
                () -> assertThat(tokenDto.getAccessToken()).isNotNull(),
                () -> assertThat(tokenDto.getRefreshToken()).isNotNull()
        );
    }

    @Test
    void 로그아웃을_한다() {
        //given
        Member member = memberRepository.save(MemberFixture.MEMBER_FIXTURE_1.create());
        LoginDto loginDto = MemberFixture.MEMBER_FIXTURE_1.login();

        TokenDto tokenDto = memberService.schoolLogin(loginDto);
        String refreshToken = tokenDto.getRefreshToken();

        // when
        memberService.logout(refreshToken);

        // then
        String redisKey = TokenProvider.REDIS_PREFIX_REFRESH + member.getId();
        assertThat(redisService.getRefreshToken(redisKey)).isNull();
    }

    @Test
    void 토큰을_재발급_받는다() {
        //given
        memberRepository.save(MemberFixture.MEMBER_FIXTURE_1.create());
        LoginDto loginDto = MemberFixture.MEMBER_FIXTURE_1.login();

        TokenDto tokenDto = memberService.schoolLogin(loginDto);
        String refreshToken = tokenDto.getRefreshToken();

        //when
        TokenDto reissueTokenDto = memberService.reissueTokens(refreshToken);

        //then
        assertAll(
                () -> assertThat(reissueTokenDto.getAccessToken()).isNotNull(),
                () -> assertThat(reissueTokenDto.getRefreshToken()).isNotNull()
        );
    }

    @Test
    void 닉네임과_FireId를_수정한다() {
        // given
        Long memberId = memberRepository.save(MemberFixture.MEMBER_FIXTURE_1.create()).getId();
        MemberUpdateNicknameDto memberUpdateNicknameDto =
                new MemberUpdateNicknameDto("newNickName", 2L);

        // when
        memberService.updateMemberNicknameFireId(memberId, memberUpdateNicknameDto);
        Member updateMember = memberRepository.findById(memberId).get();

        // then
        assertAll(
                () -> assertThat(updateMember.getNickname()).isEqualTo(memberUpdateNicknameDto.getNickname()),
                () -> assertThat(updateMember.getFireId()).isEqualTo(memberUpdateNicknameDto.getFireId())
        );
    }

    @Test
    void 닉네임은_유지하고_FireId만_수정한다() {
        //given
        Long memberId = memberRepository.save(MemberFixture.MEMBER_FIXTURE_1.create()).getId();
        MemberUpdateNicknameDto memberUpdateNicknameDto
                = new MemberUpdateNicknameDto(MemberFixture.MEMBER_FIXTURE_1.getNickName(), 2L);

        //when
        memberService.updateMemberNicknameFireId(memberId, memberUpdateNicknameDto);
        Member updateMember = memberRepository.findById(memberId).get();

        //then
        assertAll(
                () -> assertThat(updateMember.getNickname()).isEqualTo(memberUpdateNicknameDto.getNickname()),
                () -> assertThat(updateMember.getFireId()).isEqualTo(memberUpdateNicknameDto.getFireId())
        );
    }

    @Test
    void 수정_중_닉네임이_중복되면_에러를_반환한다() {
        //given
        memberRepository.save(MemberFixture.MEMBER_FIXTURE_2.create());
        Long memberId = memberRepository.save(MemberFixture.MEMBER_FIXTURE_1.create()).getId();
        MemberUpdateNicknameDto memberUpdateNicknameDto
                = new MemberUpdateNicknameDto(MemberFixture.MEMBER_FIXTURE_2.getNickName(), 1L);

        //when & then
        assertThatThrownBy(() -> memberService.updateMemberNicknameFireId(memberId, memberUpdateNicknameDto))
                .isInstanceOf(MyException.class)
                .hasMessageContaining(MyErrorCode.USER_DUPLICATE_NICKNAME.getMessage());
    }

    @Test
    void 회원_탈퇴를_한다() {
        //given
        Member member = memberRepository.save(MemberFixture.MEMBER_FIXTURE_1.create());
        LoginDto loginDto = MemberFixture.MEMBER_FIXTURE_1.login();
        String redisKey = TokenProvider.REDIS_PREFIX_REFRESH + member.getId();
        memberService.schoolLogin(loginDto);

        //when
        memberService.delete(member);

        //then
        assertAll(
                () -> assertThat(redisService.getRefreshToken(redisKey)).isNull(),
                () -> assertThat(memberRepository.findById(member.getId())).isEmpty()
        );
    }

    @Test
    void 특정_회원_정보를_조회한다() {
        // given
        Member member = memberRepository.save(MemberFixture.MEMBER_FIXTURE_1.create());

        // when
        MemberResponseDto memberResponseDto = memberService.getMember(member);

        // then
        assertAll(
                () -> assertThat(memberResponseDto).isNotNull(),
                () -> assertThat(memberResponseDto.getId()).isEqualTo(member.getId()),
                () -> assertThat(memberResponseDto.getNickname()).isEqualTo(member.getNickname()),
                () -> assertThat(memberResponseDto.getFireId()).isEqualTo(member.getFireId())
        );
    }

    @Test
    void 모든_회원_정보를_조회한다() {
        // given
        Member member1 = memberRepository.save(MemberFixture.MEMBER_FIXTURE_1.create());
        Member member2 = memberRepository.save(MemberFixture.MEMBER_FIXTURE_2.create());
        Member member3 = memberRepository.save(MemberFixture.MEMBER_FIXTURE_3.create());

        // when
        List<MemberResponseDto> memberResponseDtoList = memberService.getAllMember();
        int lastIndex = memberResponseDtoList.size() - 1;
        // then
        assertAll(
                () -> assertThat(memberResponseDtoList).isNotNull(),
                // 모든 회원 검증
                () -> {
                    MemberResponseDto dto1 = memberResponseDtoList.get(lastIndex - 2);
                    assertAll(
                            () -> assertThat(dto1.getId()).isEqualTo(member1.getId()),
                            () -> assertThat(dto1.getNickname()).isEqualTo(member1.getNickname()),
                            () -> assertThat(dto1.getFireId()).isEqualTo(member1.getFireId())
                    );
                },
                () -> {
                    MemberResponseDto dto2 = memberResponseDtoList.get(lastIndex - 1);
                    assertAll(
                            () -> assertThat(dto2.getId()).isEqualTo(member2.getId()),
                            () -> assertThat(dto2.getNickname()).isEqualTo(member2.getNickname()),
                            () -> assertThat(dto2.getFireId()).isEqualTo(member2.getFireId())
                    );
                },
                () -> {
                    MemberResponseDto dto3 = memberResponseDtoList.get(lastIndex);
                    assertAll(
                            () -> assertThat(dto3.getId()).isEqualTo(member3.getId()),
                            () -> assertThat(dto3.getNickname()).isEqualTo(member3.getNickname()),
                            () -> assertThat(dto3.getFireId()).isEqualTo(member3.getFireId())
                    );
                }
        );
    }
}