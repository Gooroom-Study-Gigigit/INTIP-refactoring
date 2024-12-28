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


    @Transactional
    public void createMember(String studentId){
        Member member = Member.builder().studentId(studentId).nickname(studentId).roles(Collections.singletonList("ROLE_USER")).build();
        memberRepository.save(member);
    }

    private TokenDto createTokens(Member member) {
        String subject = member.getId().toString();

        String accessToken = tokenProvider.createAccessToken(subject, member.getRoles());
        String refreshToken = tokenProvider.createRefreshToken(subject);

        redisService.saveRefreshToken(TokenProvider.REDIS_PREFIX_REFRESH + subject, refreshToken, tokenProvider.getRefreshTokenExpirationSeconds());

        return TokenDto.of(accessToken, refreshToken);
    }

    public TokenDto schoolLogin(LoginDto loginDto){
        if (!memberRepository.existsByStudentId(loginDto.getStudentId())) {
            createMember(loginDto.getStudentId());
        }
        Member member = memberRepository.findByStudentId(loginDto.getStudentId())
                .orElseThrow(() -> new MyException(USER_NOT_FOUND));
        return createTokens(member);
    }

    public void logout(String refreshToken) {
        String subject = tokenProvider.getUsernameByRefresh(refreshToken);
        redisService.deleteRefreshToken(TokenProvider.REDIS_PREFIX_REFRESH + subject);
    }

    public TokenDto reissueTokens(String refreshToken){
        if(!tokenProvider.validateRefreshToken(refreshToken)){
            return null;
        }
        String subject = tokenProvider.getUsernameByRefresh(refreshToken);
        String storedRefreshToken = redisService.getRefreshToken(TokenProvider.REDIS_PREFIX_REFRESH + subject);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            return null;
        }
        redisService.deleteRefreshToken(TokenProvider.REDIS_PREFIX_REFRESH + subject);
        return createTokens(memberRepository.findById(Long.valueOf(subject))
                .orElseThrow(() -> new MyException(USER_NOT_FOUND)));
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
