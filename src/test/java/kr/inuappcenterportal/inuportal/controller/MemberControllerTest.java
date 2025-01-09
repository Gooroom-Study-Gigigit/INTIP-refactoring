package kr.inuappcenterportal.inuportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import kr.inuappcenterportal.inuportal.domain.member.controller.MemberController;
import kr.inuappcenterportal.inuportal.domain.member.dto.LoginDto;
import kr.inuappcenterportal.inuportal.domain.member.dto.MemberResponseDto;
import kr.inuappcenterportal.inuportal.domain.member.dto.MemberUpdateNicknameDto;
import kr.inuappcenterportal.inuportal.domain.member.dto.TokenDto;
import kr.inuappcenterportal.inuportal.util.fixture.MemberFixture;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.service.MemberService;

import kr.inuappcenterportal.inuportal.domain.post.dto.PostListResponseDto;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.service.PostService;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyListResponseDto;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyQueryService;

import kr.inuappcenterportal.inuportal.global.config.SecurityConfig;
import kr.inuappcenterportal.inuportal.global.config.TokenProvider;
import kr.inuappcenterportal.inuportal.global.dto.ListResponseDto;
import module.ControllerTestSupport;
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

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static kr.inuappcenterportal.inuportal.global.config.TokenProvider.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockBean(JpaMetamodelMappingContext.class)
@Import(SecurityConfig.class)
public class MemberControllerTest extends ControllerTestSupport {

    public static final String MEMBER_API_BASE_PATH = "/api/members";
    private static final Long TEST_MEMBER_ID = 1L;
    private static final String TEST_ACCESS_TOKEN = "testAccessToken";
    private static final String TEST_REFRESH_TOKEN = "testRefreshToken";
    private static final long TEST_EXPIRATION = 3600;

//    private ObjectMapper objectMapper;
//
//    @Autowired
//    MockMvc mockMvc;

//    @MockBean
//    MemberService memberService;
//
//    @MockBean
//    TokenProvider tokenProvider;
//
//    @MockBean
//    private PostService postService;
//
//    @MockBean
//    private ReplyQueryService replyQueryService;

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

    @Test
    @DisplayName("회원 정보를 조회합니다.")
    void getMember() throws Exception {
        //given
        MemberResponseDto memberResponseDto = MemberResponseDto.of(MemberFixture.MEMBER_FIXTURE_1.create());
        when(memberService.getMember(any(Member.class))).thenReturn(memberResponseDto);

        //when
        ResultActions actions = mockMvc.perform(
                get(MEMBER_API_BASE_PATH)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + TEST_ACCESS_TOKEN)
        );

        //then
        verify(memberService).getMember(any(Member.class));
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("회원 가져오기 성공"))
                .andExpect(jsonPath("$.data.id").value(memberResponseDto.getId()))
                .andExpect(jsonPath("$.data.nickname").value(memberResponseDto.getNickname()))
                .andExpect(jsonPath("$.data.fireId").value(memberResponseDto.getFireId()))
                .andDo(print());
    }

    @Test
    @DisplayName("관리자는 모든 회원을 조회한다.")
    void getAllMember() throws Exception {
        // given
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(TEST_MEMBER_ID);
        when(tokenProvider.getAuthentication(TEST_ACCESS_TOKEN))
                .thenReturn(new UsernamePasswordAuthenticationToken(member, "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));


        List<Member> members = List.of(
                MemberFixture.MEMBER_FIXTURE_1.create(),
                MemberFixture.MEMBER_FIXTURE_2.create(),
                MemberFixture.MEMBER_FIXTURE_3.create()
        );
        List<MemberResponseDto> responseDtos = members.stream()
                .map(MemberResponseDto::of)
                .toList();

        when(memberService.getAllMember()).thenReturn(responseDtos);

        // when
        ResultActions actions = mockMvc.perform(
                get(MEMBER_API_BASE_PATH + "/all")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + TEST_ACCESS_TOKEN)
        );

        // then
        verify(memberService).getAllMember();
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("모든 회원 가져오기 성공"))
                .andExpect(jsonPath("$.data", hasSize(responseDtos.size())))
                .andExpect(jsonPath("$.data[0].id").value(responseDtos.get(0).getId()))
                .andExpect(jsonPath("$.data[0].nickname").value(responseDtos.get(0).getNickname()))
                .andExpect(jsonPath("$.data[0].fireId").value(responseDtos.get(0).getFireId()))
                .andExpect(jsonPath("$.data[1].id").value(responseDtos.get(1).getId()))
                .andExpect(jsonPath("$.data[1].nickname").value(responseDtos.get(1).getNickname()))
                .andExpect(jsonPath("$.data[1].fireId").value(responseDtos.get(1).getFireId()))
                .andExpect(jsonPath("$.data[2].id").value(responseDtos.get(2).getId()))
                .andExpect(jsonPath("$.data[2].nickname").value(responseDtos.get(2).getNickname()))
                .andExpect(jsonPath("$.data[2].fireId").value(responseDtos.get(2).getFireId()))
                .andDo(print());
    }

    @Test
    @DisplayName("회원이 작성한 모든 게시글을 가져온다.")
    void getAllPost() throws Exception {
        // given
        Member member = MemberFixture.MEMBER_FIXTURE_1.create();
        Post post1 = createPostFixture("testTitle1", "testContent1", "testCategory1", member);
        Post post2 = createPostFixture("testTitle2", "testContent2", "testCategory2", member);

        setBaseTimeEntityFields(post1);
        setBaseTimeEntityFields(post2);

        List<PostListResponseDto> postList = Arrays.asList(
                PostListResponseDto.of(post1, member.getNickname()),
                PostListResponseDto.of(post2, member.getNickname())
        );
        when(postService.getPostByMember(any(Member.class), any(String.class))).thenReturn(postList);

        // when
        ResultActions actions = mockMvc.perform(
                get(MEMBER_API_BASE_PATH + "/posts")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + TEST_ACCESS_TOKEN)
        );

        // then
        verify(postService).getPostByMember(any(Member.class), any(String.class));
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("회원이 작성한 모든 게시글 가져오기 성공"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(postList.get(0).getId()))
                .andExpect(jsonPath("$.data[0].title").value(postList.get(0).getTitle()))
                .andExpect(jsonPath("$.data[0].category").value(postList.get(0).getCategory()))
                .andExpect(jsonPath("$.data[0].writer").value(postList.get(0).getWriter()))
                .andExpect(jsonPath("$.data[0].content").value(postList.get(0).getContent()))
                .andExpect(jsonPath("$.data[0].createDate").value(postList.get(0).getCreateDate()))
                .andExpect(jsonPath("$.data[0].modifiedDate").value(postList.get(0).getModifiedDate()))
                .andExpect(jsonPath("$.data[1].id").value(postList.get(1).getId()))
                .andExpect(jsonPath("$.data[1].title").value(postList.get(1).getTitle()))
                .andExpect(jsonPath("$.data[1].category").value(postList.get(1).getCategory()))
                .andExpect(jsonPath("$.data[1].writer").value(postList.get(1).getWriter()))
                .andExpect(jsonPath("$.data[1].content").value(postList.get(1).getContent()))
                .andExpect(jsonPath("$.data[1].createDate").value(postList.get(1).getCreateDate()))
                .andExpect(jsonPath("$.data[1].modifiedDate").value(postList.get(1).getModifiedDate()))
                .andDo(print());
    }

    @Test
    @DisplayName("회원이 스크랩한 모든 게시글을 가져온다.")
    void getAllScrap() throws Exception {
        // given
        Member member = MemberFixture.MEMBER_FIXTURE_1.create();
        Post post1 = createPostFixture("testTitle1", "testContent1", "testCategory1", member);
        Post post2 = createPostFixture("testTitle2", "testContent2", "testCategory2", member);

        setBaseTimeEntityFields(post1);
        setBaseTimeEntityFields(post2);

        List<PostListResponseDto> postList = Arrays.asList(
                PostListResponseDto.of(post1, member.getNickname()),
                PostListResponseDto.of(post2, member.getNickname())
        );
        ListResponseDto listResponseDto = ListResponseDto.of(1L, 2L, postList);
        when(postService.getScrapsByMember(any(Member.class), any(String.class), eq(1))).thenReturn(listResponseDto);

        // when
        ResultActions actions = mockMvc.perform(
                get(MEMBER_API_BASE_PATH + "/scraps")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + TEST_ACCESS_TOKEN)
        );

        // then
        verify(postService).getScrapsByMember(any(Member.class), any(String.class), any(Integer.class));
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("회원이 스크랩한 모든 게시글 가져오기 성공"))
                .andExpect(jsonPath("$.data.pages").value(1L))
                .andExpect(jsonPath("$.data.total").value(2L))
                .andExpect(jsonPath("$.data.posts", hasSize(2)))
                .andExpect(jsonPath("$.data.posts[0].id").value(postList.get(0).getId()))
                .andExpect(jsonPath("$.data.posts[0].title").value(postList.get(0).getTitle()))
                .andExpect(jsonPath("$.data.posts[0].category").value(postList.get(0).getCategory()))
                .andExpect(jsonPath("$.data.posts[0].writer").value(postList.get(0).getWriter()))
                .andExpect(jsonPath("$.data.posts[0].content").value(postList.get(0).getContent()))
                .andExpect(jsonPath("$.data.posts[0].createDate").value(postList.get(0).getCreateDate()))
                .andExpect(jsonPath("$.data.posts[0].modifiedDate").value(postList.get(0).getModifiedDate()))
                .andExpect(jsonPath("$.data.posts[1].id").value(postList.get(1).getId()))
                .andExpect(jsonPath("$.data.posts[1].title").value(postList.get(1).getTitle()))
                .andExpect(jsonPath("$.data.posts[1].category").value(postList.get(1).getCategory()))
                .andExpect(jsonPath("$.data.posts[1].writer").value(postList.get(1).getWriter()))
                .andExpect(jsonPath("$.data.posts[1].content").value(postList.get(1).getContent()))
                .andExpect(jsonPath("$.data.posts[1].createDate").value(postList.get(1).getCreateDate()))
                .andExpect(jsonPath("$.data.posts[1].modifiedDate").value(postList.get(1).getModifiedDate()))
                .andDo(print());
    }

    @Test
    @DisplayName("회원이 좋아요한 모든 게시글을 가져온다.")
    void getAllLike() throws Exception {
        // given
        Member member = MemberFixture.MEMBER_FIXTURE_1.create();

        Post post1 = createPostFixture("testTitle1", "testContent1", "testCategory1", member);
        Post post2 = createPostFixture("testTitle2", "testContent2", "testCategory2", member);

        setBaseTimeEntityFields(post1);
        setBaseTimeEntityFields(post2);

        List<PostListResponseDto> postList = Arrays.asList(
                PostListResponseDto.of(post1, member.getNickname()),
                PostListResponseDto.of(post2, member.getNickname())
        );
        when(postService.getLikeByMember(any(Member.class), any(String.class))).thenReturn(postList);

        // when
        ResultActions actions = mockMvc.perform(
                get(MEMBER_API_BASE_PATH + "/likes")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + TEST_ACCESS_TOKEN)
        );

        // then
        verify(postService).getLikeByMember(any(Member.class), any(String.class));
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("회원이 좋아요한 모든 게시글 가져오기 성공"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(postList.get(0).getId()))
                .andExpect(jsonPath("$.data[0].title").value(postList.get(0).getTitle()))
                .andExpect(jsonPath("$.data[0].category").value(postList.get(0).getCategory()))
                .andExpect(jsonPath("$.data[0].writer").value(postList.get(0).getWriter()))
                .andExpect(jsonPath("$.data[0].content").value(postList.get(0).getContent()))
                .andExpect(jsonPath("$.data[0].createDate").value(postList.get(0).getCreateDate()))
                .andExpect(jsonPath("$.data[0].modifiedDate").value(postList.get(0).getModifiedDate()))
                .andExpect(jsonPath("$.data[1].id").value(postList.get(1).getId()))
                .andExpect(jsonPath("$.data[1].title").value(postList.get(1).getTitle()))
                .andExpect(jsonPath("$.data[1].category").value(postList.get(1).getCategory()))
                .andExpect(jsonPath("$.data[1].writer").value(postList.get(1).getWriter()))
                .andExpect(jsonPath("$.data[1].content").value(postList.get(1).getContent()))
                .andExpect(jsonPath("$.data[1].createDate").value(postList.get(1).getCreateDate()))
                .andExpect(jsonPath("$.data[1].modifiedDate").value(postList.get(1).getModifiedDate()))
                .andDo(print());
    }

    @Test
    @DisplayName("회원이 작성한 모든 댓글을 가져온다.")
    void getAllReply() throws Exception {
        // given
        Member member = MemberFixture.MEMBER_FIXTURE_1.create();

        Post post1 = createPostFixture("testTitle1", "testContent1", "testCategory1", member);
        Post post2 = createPostFixture("testTitle2", "testContent2", "testCategory2", member);

        Reply reply1 = createReplyFixture("testReplyContent1", post1, member);
        Reply reply2 = createReplyFixture("testReplyContent2", post2, member);

        setBaseTimeEntityFields(reply1);
        setBaseTimeEntityFields(reply2);

        List<ReplyListResponseDto> replyList = List.of(
                ReplyListResponseDto.of(reply1), ReplyListResponseDto.of(reply2)
        );
        when(replyQueryService.getReplyByMember(any(Member.class), any(String.class))).thenReturn(replyList);

        // when
        ResultActions actions = mockMvc.perform(
                get(MEMBER_API_BASE_PATH + "/replies")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + TEST_ACCESS_TOKEN)
        );

        // then
        verify(replyQueryService).getReplyByMember(any(Member.class), any(String.class));
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("회원이 작성한 모든 댓글 가져오기 성공"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(replyList.get(0).getId()))
                .andExpect(jsonPath("$.data[0].title").value(replyList.get(0).getTitle()))
                .andExpect(jsonPath("$.data[0].replyCount").value(replyList.get(0).getReplyCount()))
                .andExpect(jsonPath("$.data[0].content").value(replyList.get(0).getContent()))
                .andExpect(jsonPath("$.data[0].like").value(replyList.get(0).getLike()))
                .andExpect(jsonPath("$.data[0].postId").value(replyList.get(0).getPostId()))
                .andExpect(jsonPath("$.data[0].createDate").value(replyList.get(0).getCreateDate()))
                .andExpect(jsonPath("$.data[0].modifiedDate").value(replyList.get(0).getModifiedDate()))
                .andExpect(jsonPath("$.data[1].id").value(replyList.get(1).getId()))
                .andExpect(jsonPath("$.data[1].title").value(replyList.get(1).getTitle()))
                .andExpect(jsonPath("$.data[1].replyCount").value(replyList.get(1).getReplyCount()))
                .andExpect(jsonPath("$.data[1].content").value(replyList.get(1).getContent()))
                .andExpect(jsonPath("$.data[1].like").value(replyList.get(1).getLike()))
                .andExpect(jsonPath("$.data[1].postId").value(replyList.get(1).getPostId()))
                .andExpect(jsonPath("$.data[1].createDate").value(replyList.get(1).getCreateDate()))
                .andExpect(jsonPath("$.data[1].modifiedDate").value(replyList.get(1).getModifiedDate()))
                .andDo(print());
    }

    private Post createPostFixture(String title, String content, String category, Member member) {
        return Post.builder()
                .title(title)
                .content(content)
                .category(category)
                .anonymous(false)
                .member(member)
                .imageCount(1L)
                .build();
    }

    private Reply createReplyFixture(String content, Post post, Member member) {
        return Reply.builder()
                .content(content)
                .post(post)
                .member(member)
                .anonymous(false)
                .number(1L)
                .build();
    }

    private void setBaseTimeEntityFields(Object entity) throws Exception {
        Field createDateField = entity.getClass().getSuperclass().getDeclaredField("createDate");
        createDateField.setAccessible(true);
        createDateField.set(entity, LocalDate.now());

        Field modifiedDateField = entity.getClass().getSuperclass().getDeclaredField("modifiedDate");
        modifiedDateField.setAccessible(true);
        modifiedDateField.set(entity, LocalDate.now());
    }
}
