package kr.inuappcenterportal.inuportal.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.repository.MemberRepository;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyDto;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyCommandService;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyLikeService;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyQueryService;
import kr.inuappcenterportal.inuportal.global.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReplyServiceTest {
    @InjectMocks
    private ReplyCommandService replyCommandService;
    @InjectMocks
    private ReplyLikeService replyLikeService;
    @InjectMocks
    private ReplyQueryService replyQueryService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private PostRepository postRepository;

    @Mock
    private RedisService redisService;

    private Member member;
    //private Member member2;
    private Post post;

    private Reply reply;

    @BeforeEach
    void beforeEach(){
        // Mock 객체 생성 및 설정
        member = Mockito.mock(Member.class);
        post = Mockito.mock(Post.class);
        reply = Mockito.mock(Reply.class);

        // Member Mock 설정
        when(member.getId()).thenReturn(1L);
        when(member.getNickname()).thenReturn("홍길동");

        // Post Mock 설정
        when(post.getId()).thenReturn(1L);
        when(post.getMember()).thenReturn(member);
        when(post.getNumber()).thenReturn(1L);
        doNothing().when(post).upNumber(); // 익명 번호 증가
        doNothing().when(post).upReplyCount(); // 댓글 수 증가

        // Reply Mock 설정
        when(reply.getId()).thenReturn(1L);
    }

    @Test
    @DisplayName("댓글 등록 테스트 -> 게시글에 댓글을 등록(성공)")
    public void saveReplySuccessTest() throws NoSuchAlgorithmException {
        // Given
        given(postRepository.findById(any(Long.class))).willReturn(Optional.of(post));
        willDoNothing().given(redisService).blockRepeat(any(String.class));
        given(replyRepository.findFirstByPostAndMember(post, member)).willReturn(Optional.empty());
        given(replyRepository.save(any(Reply.class))).willReturn(reply);

        ReplyDto replyDto = ReplyDto.builder()
                .content("댓글 내용")
                .anonymous(true)
                .build();

        // When
        Long replyId = replyCommandService.saveReply(member, replyDto, 1L);

        // Then
        //assertNotNull(replyId);
        //assertEquals(1L, replyId);
        verify(postRepository, times(1)).findById(any(Long.class));
        verify(redisService, times(1)).blockRepeat(any(String.class));
        verify(replyRepository, times(1)).save(any(Reply.class));

        // 익명 번호 증가 로직 검증
        verify(post, times(1)).upNumber();
    }
}
