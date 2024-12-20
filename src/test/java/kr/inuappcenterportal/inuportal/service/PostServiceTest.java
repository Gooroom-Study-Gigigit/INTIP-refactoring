package kr.inuappcenterportal.inuportal.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.service.PostService;
import kr.inuappcenterportal.inuportal.domain.post.dto.PostDto;
import kr.inuappcenterportal.inuportal.domain.category.repository.CategoryRepository;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.global.service.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("새로운 게시글을 저장한다.")
    void saveOnlyPostTest() throws Exception {
        // given
        Member member = Member.builder()
                .nickname("testMember")
                .studentId("201900000")
                .roles(Collections.singletonList("ROLE_USER"))
                .build();

        PostDto postDto = PostDto.builder()
                .title("testTitle")
                .content("testContent")
                .anonymous(true)
                .category("testCategory")
                .build();

        when(categoryRepository.existsByCategory("testCategory")).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedPost, "id", 1L);
            return savedPost;
        });

        // when
        Long savedPostId = postService.saveOnlyPost(member, postDto);
        System.out.println("savedPostId = " + savedPostId);

        // then
        assertThat(savedPostId).isEqualTo(1L);
    }

    /*@Test
    @DisplayName("게시글 도배 테스트")
    public void postAttackTest() throws Exception{
        Member member = Member.builder().nickname("testMember").studentId("201900000").roles(Collections.singletonList("ROLE_USER")).build();
        PostDto postDto = PostDto.builder().title("title").content("content").anonymous(true).category("수강신청").build();
        doThrow(new MyException(MyErrorCode.BLOCK_MANY_SAME_POST_REPLY)).when(redisService).blockRepeat(any(String.class));
        *//*MyException myException = postService.saveOnlyPost(member,postDto);*//*
        Assertions.assertThrows(MyException.class, ()->postService.saveOnlyPost(member,postDto));
    }*/
}
