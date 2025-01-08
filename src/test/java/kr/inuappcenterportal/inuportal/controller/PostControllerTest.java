package kr.inuappcenterportal.inuportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.repository.MemberRepository;
import kr.inuappcenterportal.inuportal.domain.post.dto.PostDto;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.global.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MockBean
    private RedisService redisService;

    private Member testMember;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // Redis 관련 동작을 Mock
        doNothing().when(redisService).blockRepeat(anyString());

        // 데이터 초기화
        postRepository.deleteAll();
        memberRepository.deleteAll();

        // 테스트용 Member 생성 및 SecurityContext에 주입
        testMember = Member.builder()
                .studentId("2019015")
                .nickname("TestUser")
                .roles(Collections.singletonList("ROLE_USER"))
                .build();
        memberRepository.save(testMember);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        testMember, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    @Test
    @DisplayName("게시글 저장")
    void savePost() throws Exception {
        // given
        PostDto postDto = PostDto.builder()
                .title("Integration Test Title")
                .content("Integration Test Content")
                .category("수강신청")
                .anonymous(true)
                .build();
        String postJson = objectMapper.writeValueAsString(postDto);

        // when
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value("게시글 등록 성공"));

        // then
        Post savedPost = postRepository.findAll().get(0);
        assertThat(savedPost.getTitle()).isEqualTo("Integration Test Title");
        assertThat(savedPost.getContent()).isEqualTo("Integration Test Content");
        assertThat(savedPost.getCategory()).isEqualTo("수강신청");
        assertThat(savedPost.getMember()).isNotNull();
    }

    @Test
    @DisplayName("게시글 조회")
    void getPost() throws Exception {
        // given
        Post post = Post.builder()
                .title("Test Title")
                .content("Test Content")
                .category("수강신청")
                .anonymous(false)
                .member(testMember)
                .imageCount(0)
                .build();
        postRepository.save(post);

        // when & then
        mockMvc.perform(get("/api/posts/{postId}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("게시글 가져오기 성공"))
                .andExpect(jsonPath("$.data.title").value("Test Title"))
                .andExpect(jsonPath("$.data.content").value("Test Content"));
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost() throws Exception {
        // given
        Post post = Post.builder()
                .title("Test Title")
                .content("Test Content")
                .category("수강신청")
                .anonymous(false)
                .member(testMember)
                .imageCount(0)
                .build();
        postRepository.save(post);

        // when
        mockMvc.perform(delete("/api/posts/{postId}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("게시글 삭제 성공"));

        // then
        assertThat(postRepository.findById(post.getId())).isEmpty();
    }
}
