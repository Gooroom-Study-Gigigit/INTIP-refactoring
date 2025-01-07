package kr.inuappcenterportal.inuportal.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyDto;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyCommandService;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import kr.inuappcenterportal.inuportal.global.service.RedisService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static kr.inuappcenterportal.inuportal.util.TestReflectionUtil.setField;
import static kr.inuappcenterportal.inuportal.util.TestReflectionUtil.setId;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReplyCommandServiceTest {
    @InjectMocks
    private ReplyCommandService replyCommandService;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private RedisService redisService;

    @Test
    @DisplayName("댓글 등록 테스트 -> 다른 사람 게시글에 댓글을 달기(성공)")
    public void saveReplySuccess1Test() throws NoSuchAlgorithmException {
        // given
        Long postId = 1L;
        Long postMemberId = 1L;
        Long replyId = 1L;
        Long reqMemberId = 2L;

        // 객체 생성 및 설정
        Member reqMember = Member.builder().build();
        setId(reqMember, reqMemberId);

        Member postOwner = Member.builder().build();
        setId(postOwner, postMemberId);

        Post post = Post.builder().member(postOwner).build();
        setId(post, postId);

        Reply reply = Reply.builder().build();
        setId(reply, replyId);

        ReplyDto replyDto = ReplyDto.builder().content("댓글 내용").anonymous(true).build();

        // Post 설정
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // Redis 중복 요청 방지 설정
        willDoNothing().given(redisService).blockRepeat(anyString());

        // Reply Repository 설정
        given(replyRepository.findFirstByPostAndMember(post, reqMember)).willReturn(Optional.empty());
        given(replyRepository.save(any(Reply.class))).willReturn(reply);

        // when
        Long savedReplyId = replyCommandService.saveReply(reqMember, replyDto, postId);

        // then
        Assertions.assertEquals(replyId, savedReplyId);
        Assertions.assertEquals(1L, post.getReplyCount());
        verify(postRepository, times(1)).findById(postId);
        verify(replyRepository, times(1)).findFirstByPostAndMember(post, reqMember);
        verify(replyRepository, times(1)).save(any(Reply.class));
    }

    @Test
    @DisplayName("댓글 등록 테스트 -> 자기 자신의 게시글에 댓글 달기(성공)")
    public void saveReplySuccess2Test() throws NoSuchAlgorithmException {
        // given
        Long postId = 1L;
        Long reqMemberId = 1L;
        Long replyId = 1L;

        // 객체 생성 및 설정
        Member reqMember = Member.builder().build();
        setId(reqMember, reqMemberId);

        Member postOwner = Member.builder().build();
        setId(postOwner, reqMemberId);

        Post post = Post.builder().member(postOwner).build();
        setId(post, postId);

        Reply reply = Reply.builder().build();
        setId(reply, replyId);

        ReplyDto replyDto = ReplyDto.builder().content("댓글 내용").anonymous(true).build();

        // Post 설정
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // Redis 중복 요청 방지 설정
        willDoNothing().given(redisService).blockRepeat(anyString());

        // Reply Repository 설정
        given(replyRepository.save(any(Reply.class))).willReturn(reply);

        // when
        Long savedReplyId = replyCommandService.saveReply(reqMember, replyDto, postId);

        // then
        Assertions.assertEquals(replyId, savedReplyId);
        Assertions.assertEquals(1L, post.getReplyCount());
        verify(postRepository, times(1)).findById(postId);
        verify(replyRepository, times(1)).save(any(Reply.class));
    }

    @Test
    @DisplayName("댓글 등록 테스트 -> 없는 게시글에 댓글을 달기(실패)")
    public void saveReplyFail1Test() throws NoSuchAlgorithmException {
        // given
        Long nonExistPostId = 100L;
        ReplyDto replyDto = ReplyDto.builder().content("댓글 내용").anonymous(true).build();

        // 객체 생성 및 설정
        Member member = Member.builder().build();

        // PostRepository 설정
        when(postRepository.findById(nonExistPostId)).thenReturn(Optional.empty());

        // when
        MyException exception = assertThrows(MyException.class, () -> {
            replyCommandService.saveReply(member, replyDto, nonExistPostId);
        });

        // then
        Assertions.assertEquals(MyErrorCode.POST_NOT_FOUND, exception.getErrorCode());
        verify(postRepository, times(1)).findById(nonExistPostId);
        verifyNoInteractions(replyRepository); // ReplyRepository는 호출되지 않아야 함
    }

    @Test
    @DisplayName("대댓글 등록 테스트 -> 다른 사람 댓글에 대댓글을 달기(성공)")
    public void saveReReplySuccess1Test() throws NoSuchAlgorithmException {
        // given
        Long parentReplyId = 1L;
        Long postId = 1L;
        Long postMemberId = 1L;
        Long reqMemberId = 2L;
        Long reReplyId = 2L;

        // 객체 생성 및 설정
        Member reqMember = Member.builder().build();
        setId(reqMember, reqMemberId);

        Member postOwner = Member.builder().build();
        setId(postOwner, postMemberId);

        Post post = Post.builder().member(postOwner).build();
        setId(post, postId);
        setField(post, "replyCount", 1L);

        Reply parentReply = Reply.builder().post(post).reply(null).build();
        setId(parentReply, parentReplyId);

        Reply reReply = Reply.builder().build();
        setId(reReply, reReplyId);

        ReplyDto replyDto = ReplyDto.builder().content("대댓글 내용").anonymous(true).build();

        // Parent Reply 설정
        given(replyRepository.findById(parentReplyId)).willReturn(Optional.of(parentReply));

        // Post 설정
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // Redis 중복 요청 방지 설정
        willDoNothing().given(redisService).blockRepeat(anyString());

        // Reply Repository 설정
        given(replyRepository.findFirstByPostAndMember(post, reqMember)).willReturn(Optional.empty());
        given(replyRepository.save(any(Reply.class))).willReturn(reReply);

        // when
        Long savedReplyId = replyCommandService.saveReReply(reqMember, replyDto, parentReplyId);

        // then
        Assertions.assertEquals(reReplyId, savedReplyId);
        Assertions.assertEquals(2L, post.getReplyCount());
        verify(postRepository, times(1)).findById(postId);
        verify(replyRepository, times(1)).findById(parentReplyId);
        verify(replyRepository, times(1)).findFirstByPostAndMember(post, reqMember);
        verify(replyRepository, times(1)).save(any(Reply.class));
    }

    @Test
    @DisplayName("대댓글 등록 테스트 -> 자기 자신에게 대댓글을 달기(성공)")
    public void saveReReplySuccessTest2() throws NoSuchAlgorithmException {
        // given
        Long parentReplyId = 1L;
        Long postId = 1L;
        Long reqMemberId = 1L; // 자기 자신
        Long reReplyId = 2L;

        // 객체 생성 및 설정
        Member reqMember = Member.builder().build();
        setId(reqMember, reqMemberId);

        Post post = Post.builder().member(reqMember).build();
        setId(post, postId);
        setField(post,"replyCount", 1L);

        Reply parentReply = Reply.builder().post(post).reply(null).build();
        setId(parentReply, parentReplyId);

        Reply reReply = Reply.builder().build();
        setId(reReply, reReplyId);

        ReplyDto replyDto = ReplyDto.builder().content("대댓글 내용").anonymous(true).build();

        // Parent Reply 설정
        given(replyRepository.findById(parentReplyId)).willReturn(Optional.of(parentReply));

        // Post 설정
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // Redis 중복 요청 방지 설정
        willDoNothing().given(redisService).blockRepeat(anyString());

        // Reply Repository 설정
        given(replyRepository.save(any(Reply.class))).willReturn(reReply);

        // when
        Long savedReplyId = replyCommandService.saveReReply(reqMember, replyDto, parentReplyId);

        // then
        Assertions.assertEquals(reReplyId, savedReplyId);
        Assertions.assertEquals(2L, post.getReplyCount());
        verify(postRepository, times(1)).findById(postId);
        verify(replyRepository, times(1)).findById(parentReplyId);
        verify(replyRepository, times(1)).save(any(Reply.class));
    }
    @Test
    @DisplayName("대댓글 등록 테스트 -> 대댓글에 대댓글을 달기(실패)")
    public void saveReReplyFail1Test() throws NoSuchAlgorithmException {
        // given
        Long targetReplyId = 1L;
        Long reqMemberId = 1L; // 자기 자신

        // 객체 생성 및 설정
        Member reqMember = Member.builder().build();
        setId(reqMember, reqMemberId);

        Reply targetReply = Reply.builder().reply(Reply.builder().build()).build(); // 이미 대댓글
        setId(targetReply, targetReplyId);

        ReplyDto replyDto = ReplyDto.builder().content("대댓글 내용").anonymous(true).build();

        // Parent Reply 설정
        given(replyRepository.findById(targetReplyId)).willReturn(Optional.of(targetReply));

        // Redis 중복 요청 방지 설정
        willDoNothing().given(redisService).blockRepeat(anyString());

        // when
        MyException exception = assertThrows(MyException.class, () -> {
            replyCommandService.saveReReply(reqMember, replyDto, targetReplyId);
        });

        // then
        Assertions.assertEquals(MyErrorCode.NOT_REPLY_ON_REREPLY, exception.getErrorCode());
        verify(replyRepository, times(1)).findById(targetReplyId);
        verifyNoInteractions(postRepository);
    }
    @Test
    @DisplayName("대댓글 등록 테스트 -> 없는 댓글에 대댓글 달기(실패)")
    public void saveReReplyFail2Test() throws NoSuchAlgorithmException {
        // given
        Long targetReplyId = 1L;

        // Mock 객체 생성 및 설정
        Member reqMember = mock(Member.class);
        ReplyDto replyDto = ReplyDto.builder().content("대댓글 내용").anonymous(true).build();

        // when
        MyException exception = assertThrows(MyException.class, () -> {
            replyCommandService.saveReReply(reqMember, replyDto, targetReplyId);
        });

        // then
        Assertions.assertEquals(MyErrorCode.REPLY_NOT_FOUND, exception.getErrorCode());
        verify(replyRepository, times(1)).findById(targetReplyId);
        verifyNoInteractions(postRepository);
    }
    @Test
    @DisplayName("댓글 수정 테스트 -> 본인의 댓글 수정(성공)")
    public void updateReplySuccessTest() {
        // given
        Long memberId = 1L;
        Long replyId = 1L;
        ReplyDto replyDto = ReplyDto.builder().content("수정된 댓글 내용").anonymous(true).build();

        // 객체 생성 및 설정
        Member member = Member.builder().build();
        setId(member, memberId);

        Reply reply = Reply.builder().content("댓글 내용").anonymous(false).member(member).build();
        setId(reply, replyId);

        given(replyRepository.findById(replyId)).willReturn(Optional.of(reply));

        // when
        Long updatedReplyId = replyCommandService.updateReply(memberId, replyDto, replyId);

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals(replyId, updatedReplyId, "수정된 댓글 ID가 일치해야 합니다."),
                () -> Assertions.assertEquals("수정된 댓글 내용", reply.getContent(), "댓글 내용이 수정되었는지 확인합니다."),
                () -> Assertions.assertTrue(reply.getAnonymous(), "익명 여부가 수정되었는지 확인합니다.")
        );
        verify(replyRepository, times(1)).findById(replyId);
    }
    @Test
    @DisplayName("댓글 수정 테스트 -> 본인이 아닌 댓글 수정 시도(실패)")
    public void updateReplyFailTest() {
        // given
        Long memberId = 1L;
        Long anotherMemberId = 2L; // 댓글 작성자가 아님
        Long replyId = 2L;
        ReplyDto replyDto = ReplyDto.builder().content("수정된 댓글 내용").anonymous(true).build();

        // 객체 생성 및 설정
        Member anotherMember = Member.builder().build();
        setId(anotherMember, anotherMemberId);

        Reply reply = Reply.builder().content("댓글 내용").anonymous(false).member(anotherMember).build();
        setId(reply, replyId);

        given(replyRepository.findById(replyId)).willReturn(Optional.of(reply));

        // when
        MyException exception = assertThrows(MyException.class, () -> {
            replyCommandService.updateReply(memberId, replyDto, replyId);
        });

        // then
        Assertions.assertEquals(MyErrorCode.HAS_NOT_REPLY_AUTHORIZATION, exception.getErrorCode());
        verify(replyRepository, times(1)).findById(replyId);
    }

    @Test
    @DisplayName("댓글 삭제 테스트 -> 본인의 댓글 삭제(성공)")
    public void deleteReplySuccessTest() {
        // given
        Long memberId = 1L;
        Long replyId = 1L;
        Long postId = 1L;

        // 객체 생성 및 설정
        Member member = Member.builder().build();
        setId(member, memberId);

        Post post = Post.builder().build();
        setId(post, postId);
        setField(post,"replyCount",1L);

        Reply reply = Reply.builder().post(post).member(member).build();
        setId(reply, replyId);

        given(replyRepository.findById(replyId)).willReturn(Optional.of(reply));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        replyCommandService.delete(memberId, replyId);

        // then
        Assertions.assertEquals(0L, post.getReplyCount());
        Assertions.assertTrue(reply.getIsDeleted());
        verify(replyRepository, times(1)).findById(replyId);
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    @DisplayName("댓글 삭제 테스트 -> 본인이 아닌 댓글 삭제 시도(실패)")
    public void deleteReplyFailTest() {
        // given
        Long memberId = 1L;
        Long replyId = 1L;
        Long anotherMemberId = 2L;
        Long postId = 1L;

        // 객체 생성 및 설정
        Member anotherMember = Member.builder().build();
        setId(anotherMember, anotherMemberId);

        Post post = Post.builder().build();
        setId(post, postId);

        Reply reply = Reply.builder().post(post).member(anotherMember).build();
        setId(reply, replyId);

        given(replyRepository.findById(replyId)).willReturn(Optional.of(reply));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        MyException exception = assertThrows(MyException.class, () -> {
            replyCommandService.delete(memberId, replyId);
        });

        // then
        Assertions.assertEquals(MyErrorCode.HAS_NOT_REPLY_AUTHORIZATION, exception.getErrorCode());
        verify(replyRepository, times(1)).findById(replyId);
        verify(postRepository, times(1)).findById(postId);
    }

}
