package kr.inuappcenterportal.inuportal.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import kr.inuappcenterportal.inuportal.domain.member.dto.LoginDto;
import kr.inuappcenterportal.inuportal.domain.member.dto.MemberUpdateNicknameDto;
import kr.inuappcenterportal.inuportal.domain.member.dto.TokenDto;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.service.MemberService;
import kr.inuappcenterportal.inuportal.domain.post.service.PostService;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyQueryService;
import kr.inuappcenterportal.inuportal.global.config.SecurityConfig;
import kr.inuappcenterportal.inuportal.global.config.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static kr.inuappcenterportal.inuportal.global.config.TokenProvider.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@MockBean(JpaMetamodelMappingContext.class)
@Import(SecurityConfig.class)
public class MemberControllerTest {

    public static final String MEMBER_API_BASE_PATH = "/api/members";
    private static final Long TEST_MEMBER_ID = 1L;
    private static final String TEST_ACCESS_TOKEN = "testAccessToken";
    private static final String TEST_REFRESH_TOKEN = "testRefreshToken";
    private static final long TEST_EXPIRATION = 3600;

    private ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MemberService memberService;

    @MockBean
    TokenProvider tokenProvider;

    @MockBean
    private PostService postService;

    @MockBean
    private ReplyQueryService replyQueryService;

    @BeforeEach
    void setUp() {
        objectMapper=  new ObjectMapper();
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(TEST_MEMBER_ID);

        when(tokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(TEST_ACCESS_TOKEN);
        when(tokenProvider.validateToken(TEST_ACCESS_TOKEN)).thenReturn(true);
        when(tokenProvider.getAuthentication(TEST_ACCESS_TOKEN))
                .thenReturn(new UsernamePasswordAuthenticationToken(member, "", List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        when(tokenProvider.getRefreshTokenExpirationSeconds()).thenReturn(TEST_EXPIRATION);
    }

    @Test
    @DisplayName("로그인 테스트")
    void login() throws Exception {
        //given
        String requestBody = objectMapper.writeValueAsString(new LoginDto("20250101", "1234"));
        when(memberService.schoolLogin(any(LoginDto.class))).thenReturn(TokenDto.of(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN));

        //when
        ResultActions actions = mockMvc.perform(
                post(MEMBER_API_BASE_PATH + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        //then
        verify(memberService).schoolLogin(any(LoginDto.class));
        actions
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_ACCESS_TOKEN))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=" + TEST_REFRESH_TOKEN)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=" + TEST_EXPIRATION)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
                .andExpect(jsonPath("$.msg").value("로그인 성공, 토큰이 발급되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void logout() throws Exception {
        //given
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, TEST_REFRESH_TOKEN);
        doNothing().when(memberService).logout(TEST_REFRESH_TOKEN);

        //when
        ResultActions actions = mockMvc.perform(
                post(MEMBER_API_BASE_PATH + "/logout")
                        .cookie(refreshTokenCookie)
        );

        //then
        verify(memberService).logout(TEST_REFRESH_TOKEN);
        actions
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
                .andExpect(jsonPath("$.msg").value("로그아웃 성공"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 삭제 테스트")
    void deleteMember() throws Exception {
        //given
        doNothing().when(memberService).delete(any());

        //when
        ResultActions actions = mockMvc.perform(
                delete(MEMBER_API_BASE_PATH)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + TEST_ACCESS_TOKEN)
        );

        //then
        verify(memberService).delete(any());
        actions
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
                .andExpect(jsonPath("$.msg").value("회원 삭제 성공"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    @Test
    @DisplayName("토큰 재발급 테스트")
    void reissue() throws Exception {
        //given
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, TEST_REFRESH_TOKEN);
        when(memberService.reissueTokens(TEST_REFRESH_TOKEN)).thenReturn(TokenDto.of(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN));

        //when
        ResultActions actions = mockMvc.perform(
                post(MEMBER_API_BASE_PATH + "/refresh")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + TEST_ACCESS_TOKEN)
                        .cookie(refreshTokenCookie)
        );

        //then
        verify(memberService).reissueTokens(TEST_REFRESH_TOKEN);
        actions
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION,  BEARER_PREFIX + TEST_ACCESS_TOKEN))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=" + TEST_REFRESH_TOKEN)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=" + TEST_EXPIRATION)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
                .andExpect(jsonPath("$.msg").value("토큰 재발급 성공"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    @Test
    @DisplayName("닉네임/횃불 이미지 변경 테스트")
    void updateNicknameFireId() throws Exception {
        //given
        MemberUpdateNicknameDto updateDto = new MemberUpdateNicknameDto("newNickname", 2L);
        String requestBody = objectMapper.writeValueAsString(updateDto);
        when(memberService.updateMemberNicknameFireId(anyLong(), any(MemberUpdateNicknameDto.class))).thenReturn(TEST_MEMBER_ID);

        //when
        ResultActions actions = mockMvc.perform(
                put(MEMBER_API_BASE_PATH)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + TEST_ACCESS_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        //then
        verify(memberService).updateMemberNicknameFireId(anyLong(), any(MemberUpdateNicknameDto.class));
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("회원 닉네임/횃불이 이미지 변경 성공"))
                .andExpect(jsonPath("$.data").value(TEST_MEMBER_ID))
                .andDo(print());
    }
}
