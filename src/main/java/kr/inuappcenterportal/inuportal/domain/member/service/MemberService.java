package kr.inuappcenterportal.inuportal.domain.member.service;

import kr.inuappcenterportal.inuportal.global.config.TokenProvider;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.dto.LoginDto;
import kr.inuappcenterportal.inuportal.domain.member.dto.MemberResponseDto;
import kr.inuappcenterportal.inuportal.domain.member.dto.MemberUpdateNicknameDto;
import kr.inuappcenterportal.inuportal.domain.member.dto.TokenDto;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import kr.inuappcenterportal.inuportal.domain.member.repository.SchoolLoginRepository;
import kr.inuappcenterportal.inuportal.domain.member.repository.MemberRepository;
import kr.inuappcenterportal.inuportal.global.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final SchoolLoginRepository schoolLoginRepository;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;

    private static final String REDIS_PREFIX_REFRESH = "RT:";

    private static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 1000L * 60 * 60 * 2 ;//2시간
    private static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 1000L * 60 * 60 * 24;

    @Transactional
    public void createMember(String studentId){
        Member member = Member.builder().studentId(studentId).nickname(studentId).roles(Collections.singletonList("ROLE_USER")).build();
        memberRepository.save(member);
    }

    private TokenDto createTokens(Member member) {
        String subject = member.getId().toString();

        String accessToken = tokenProvider.createAccessToken(subject, member.getRoles(), ACCESS_TOKEN_EXPIRATION_SECONDS);
        String refreshToken = tokenProvider.createRefreshToken(subject, REFRESH_TOKEN_EXPIRATION_SECONDS);

        redisService.saveRefreshToken(REDIS_PREFIX_REFRESH + subject, refreshToken, REFRESH_TOKEN_EXPIRATION_SECONDS);

        return TokenDto.of(accessToken, refreshToken);
    }

    @Transactional
    public TokenDto schoolLogin(LoginDto loginDto){
        if (!memberRepository.existsByStudentId(loginDto.getStudentId())) {
            createMember(loginDto.getStudentId());
        }
        return createTokens(memberRepository.findByStudentId(loginDto.getStudentId())
                .orElseThrow(() -> new MyException(USER_NOT_FOUND)));
    }

    public TokenDto refreshToken(String token){
        if(!tokenProvider.validateRefreshToken(token)){
            throw new MyException(EXPIRED_TOKEN);
        }
        Long id = Long.valueOf(tokenProvider.getUsernameByRefresh(token));
        Member member = memberRepository.findById(id).orElseThrow(()->new MyException(USER_NOT_FOUND));
        return createTokens(member);
    }

    @Transactional
    public Long updateMemberNicknameFireId(Long id, MemberUpdateNicknameDto memberUpdateNicknameDto){
        Member member = memberRepository.findById(id).orElseThrow(
                ()->new MyException(USER_NOT_FOUND));
        if(memberUpdateNicknameDto.getNickname()!=null) {
            if (memberRepository.existsByNickname(memberUpdateNicknameDto.getNickname())) {
                throw new MyException(USER_DUPLICATE_NICKNAME);
            }
            if(memberUpdateNicknameDto.getNickname().trim().isEmpty()){
                throw new MyException(NOT_BLANK_NICKNAME);
            }
            if(memberUpdateNicknameDto.getFireId()!=null){
                member.updateNicknameAndFire(memberUpdateNicknameDto.getNickname(),memberUpdateNicknameDto.getFireId());
            }
            else{
                member.updateNickName(memberUpdateNicknameDto.getNickname());
            }
        }else if(memberUpdateNicknameDto.getFireId()!=null){
            member.updateFire(memberUpdateNicknameDto.getFireId());
        }
        else{
            throw new MyException(EMPTY_REQUEST);
        }
        return member.getId();
    }

    @Transactional
    public void delete(Member member){
        memberRepository.delete(member);
    }

    public MemberResponseDto getMember(Member member){
        return MemberResponseDto.of(member);
    }

    public List<MemberResponseDto> getAllMember(){
        return memberRepository.findAll().stream().map(MemberResponseDto::of).collect(Collectors.toList());
    }
}
