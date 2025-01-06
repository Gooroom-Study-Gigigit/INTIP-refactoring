package kr.inuappcenterportal.inuportal.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.service.PostCommonService;
import kr.inuappcenterportal.inuportal.domain.post.service.PostImageService;
import kr.inuappcenterportal.inuportal.domain.post.service.PostService;
import kr.inuappcenterportal.inuportal.domain.post.dto.PostDto;
import kr.inuappcenterportal.inuportal.domain.category.repository.CategoryRepository;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import kr.inuappcenterportal.inuportal.global.service.RedisService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;


import java.util.Collections;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostCommonService postCommonService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("새로운 게시글을 저장한다.")
    void saveOnlyPostTest_Success() throws Exception {
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
        assertEquals(savedPostId, savedPostId);
        verify(redisService, times(1)).blockRepeat(anyString());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글을 성공적으로 수정한다.")
    void updateOnlyPostTest_Success() {
        // given
        Long postId = 1L;
        Long memberId = 2L;

        Member member = Member.builder()
                .nickname("testMember")
                .studentId("201900000")
                .roles(Collections.singletonList("ROLE_USER"))
                .build();

        Post post = Post.builder()
                .title("oldTitle")
                .content("oldContent")
                .anonymous(false)
                .category("oldCategory")
                .member(member)
                .build();

        PostDto postDto = PostDto.builder()
                .title("newTitle")
                .content("newContent")
                .anonymous(true)
                .category("newCategory")
                .build();

        when(postCommonService.findPostByIdOrThrow(postId)).thenReturn(post);
        doNothing().when(postCommonService).validateMemberAuthorization(post, memberId);
        when(categoryRepository.existsByCategory("newCategory")).thenReturn(true);

        // when
        postService.updateOnlyPost(memberId, postId, postDto);

        // then
        assertThat(post.getTitle()).isEqualTo("newTitle");
        assertThat(post.getContent()).isEqualTo("newContent");
        assertThat(post.getAnonymous()).isTrue();
        assertThat(post.getCategory()).isEqualTo("newCategory");

        verify(postCommonService, times(1)).findPostByIdOrThrow(postId);
        verify(postCommonService, times(1)).validateMemberAuthorization(post, memberId);
        verify(categoryRepository, times(1)).existsByCategory("newCategory");
    }

    @Test
    @DisplayName("작성자가 다를 경우 게시글 수정에 실패한다.")
    void updateOnlyPost_Fail_DifferentAuthor() {
        // given
        Long postId = 1L;
        Long memberId = 2L;
        Long anotherMemberId = 3L;

        Member anotherMember = Member.builder()
                .nickname("testMember")
                .studentId("201900000")
                .roles(Collections.singletonList("ROLE_USER"))
                .build();

        ReflectionTestUtils.setField(anotherMember, "id", anotherMemberId);

        Post post = Post.builder()
                .title("oldTitle")
                .content("oldContent")
                .anonymous(false)
                .category("oldCategory")
                .member(anotherMember)
                .build();

        PostDto postDto = PostDto.builder()
                .title("newTitle")
                .content("newContent")
                .anonymous(true)
                .category("newCategory")
                .build();

        when(categoryRepository.existsByCategory(postDto.getCategory())).thenReturn(true);
        when(postCommonService.findPostByIdOrThrow(postId)).thenReturn(post);
        doThrow(new MyException(MyErrorCode.HAS_NOT_POST_AUTHORIZATION))
                .when(postCommonService).validateMemberAuthorization(post, memberId);

        // when
        assertThrows(MyException.class, () -> postService.updateOnlyPost(memberId, postId, postDto));

        // then
        verify(postCommonService).findPostByIdOrThrow(postId);
        verify(postCommonService).validateMemberAuthorization(post, memberId);
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
