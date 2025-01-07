package kr.inuappcenterportal.inuportal.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyLikeService;
import kr.inuappcenterportal.inuportal.domain.replylike.model.LikeAction;
import kr.inuappcenterportal.inuportal.domain.replylike.model.ReplyLike;
import kr.inuappcenterportal.inuportal.domain.replylike.repository.ReplyLikeRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static kr.inuappcenterportal.inuportal.util.TestReflectionUtil.setField;
import static kr.inuappcenterportal.inuportal.util.TestReflectionUtil.setId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReplyLikeServiceTest {

    @InjectMocks
    private ReplyLikeService replyLikeService;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private ReplyLikeRepository likeReplyRepository;

    @Test
    @DisplayName("댓글에 좋아요 처리하기 - 좋아요 처리 하지 않은 댓글에 좋아요 처리 하기(성공)")
    void likeReplySuccess1Test() {
        // given
        Long targetReplyId = 1L;
        Long replyOwnerId = 1L;
        Long reqMemberId = 2L;

        // 객체 생성 및 설정
        Member reqMember = Member.builder().build();
        setId(reqMember, reqMemberId);

        Member replyOwner = Member.builder().build();
        setId(replyOwner, replyOwnerId);

        Reply targetReply = Reply.builder().member(replyOwner).build();
        setId(targetReply, targetReplyId);

        ReplyLike replyLike = ReplyLike.builder().build();

        // Reply 설정
        given(replyRepository.findByIdWithLock(targetReplyId)).willReturn(Optional.of(targetReply));

        // 좋아요 확인 설정
        given(likeReplyRepository.findByMemberAndReply(reqMember, targetReply)).willReturn(Optional.empty());

        // 좋아요 엔티티 저장 동작 설정
        given(likeReplyRepository.save(any(ReplyLike.class))).willReturn(replyLike);

        // when
        LikeAction likeAction = replyLikeService.likeReply(reqMember, targetReplyId);

        // then
        assertEquals(LikeAction.LIKE, likeAction);
        assertEquals(1L, targetReply.getLikeCount(), "좋아요 수 증가 확인하기");
        verify(likeReplyRepository, times(1)).save(any(ReplyLike.class));
        verify(replyRepository, times(1)).findByIdWithLock(targetReplyId);
        verify(likeReplyRepository, times(1)).findByMemberAndReply(reqMember, targetReply);
    }
    @Test
    @DisplayName("댓글에 좋아요 처리하기 - 좋아요 처리한 댓글에 좋아요 취소 하기(성공)")
    void likeReplySuccess2Test() {
        // given
        Long targetReplyId = 1L;
        Long replyOwnerId = 1L;
        Long reqMemberId = 2L;

        // 객체 생성 및 설정
        Member reqMember = Member.builder().build();
        setId(reqMember, reqMemberId);

        Member replyOwner = Member.builder().build();
        setId(replyOwner, replyOwnerId);

        Reply targetReply = Reply.builder().member(replyOwner).build();
        setId(targetReply, targetReplyId);
        setField(targetReply,"likeCount",1L);

        ReplyLike replyLike = ReplyLike.builder().reply(targetReply).build();

        // Reply 설정
        given(replyRepository.findByIdWithLock(targetReplyId)).willReturn(Optional.of(targetReply));

        // 좋아요 확인 설정
        given(likeReplyRepository.findByMemberAndReply(reqMember, targetReply)).willReturn(Optional.of(replyLike));

        // 좋아요 엔티티 삭제 동작 설정
        doNothing().when(likeReplyRepository).delete(any(ReplyLike.class));

        // when
        LikeAction likeAction = replyLikeService.likeReply(reqMember, targetReplyId);

        // then
        assertEquals(LikeAction.UNLIKE, likeAction);
        assertEquals(0L, targetReply.getLikeCount(), "좋아요 수 감소 확인하기");
        verify(likeReplyRepository, times(1)).delete(any(ReplyLike.class));
        verify(replyRepository, times(1)).findByIdWithLock(targetReplyId);
        verify(likeReplyRepository, times(1)).findByMemberAndReply(reqMember, targetReply);
    }
    @Test
    @DisplayName("댓글에 좋아요 처리하기 - 자기 자신 댓글에 좋아요 처리(실패)")
    void likeReplyFail1Test() {
        // given
        Long targetReplyId = 1L;
        Long replyOwnerId = 1L;
        Long reqMemberId = 1L;

        // 객체 생성 및 설정
        Member reqMember = Member.builder().build();
        setId(reqMember, reqMemberId);

        Member replyOwner = Member.builder().build();
        setId(replyOwner, replyOwnerId);

        Reply targetReply = Reply.builder().member(replyOwner).build();
        setId(targetReply, targetReplyId);

        // Reply 설정
        given(replyRepository.findByIdWithLock(targetReplyId)).willReturn(Optional.of(targetReply));

        // when
        MyException exception = assertThrows(MyException.class, () -> {
            replyLikeService.likeReply(reqMember, targetReplyId);
        });

        // then
        assertEquals(MyErrorCode.NOT_LIKE_MY_REPLY, exception.getErrorCode());
        verify(replyRepository, times(1)).findByIdWithLock(targetReplyId);
        verifyNoInteractions(likeReplyRepository);
    }

    @Test
    @DisplayName("댓글에 좋아요 처리하기 - 없는 댓글에 좋아요 처리(실패)")
    void likeReplyFail2Test() {
        // given
        Long targetReplyId = 100L;

        // 객체 생성 및 설정
        Member reqMember = Member.builder().build();

        // Reply 설정
        given(replyRepository.findByIdWithLock(targetReplyId)).willReturn(Optional.empty());

        // when
        MyException exception = assertThrows(MyException.class, () -> {
            replyLikeService.likeReply(reqMember, targetReplyId);
        });

        // then
        assertEquals(MyErrorCode.REPLY_NOT_FOUND, exception.getErrorCode());
        verify(replyRepository, times(1)).findByIdWithLock(targetReplyId);
        verifyNoInteractions(likeReplyRepository);
    }


}
