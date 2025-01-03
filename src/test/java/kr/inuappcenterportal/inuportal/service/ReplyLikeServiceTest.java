package kr.inuappcenterportal.inuportal.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyLikeService;
import kr.inuappcenterportal.inuportal.domain.replylike.model.LikeAction;
import kr.inuappcenterportal.inuportal.domain.replylike.model.ReplyLike;
import kr.inuappcenterportal.inuportal.domain.replylike.repository.LikeReplyRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

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
    private LikeReplyRepository likeReplyRepository;

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
        given(replyRepository.findById(targetReplyId)).willReturn(Optional.of(targetReply));

        // 좋아요 확인 설정
        given(likeReplyRepository.findByMemberAndReply(reqMember, targetReply)).willReturn(Optional.empty());

        // 좋아요 엔티티 저장 동작 설정
        given(likeReplyRepository.save(any(ReplyLike.class))).willReturn(replyLike);

        // when
        LikeAction likeAction = replyLikeService.likeReply(reqMember, targetReplyId);

        // then
        Assertions.assertEquals(LikeAction.LIKE, likeAction);
        Assertions.assertEquals(1L, targetReply.getLikeCount(), "좋아요 수 증가 확인하기");
        verify(likeReplyRepository, times(1)).save(any(ReplyLike.class));
        verify(replyRepository, times(1)).findById(targetReplyId);
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
        given(replyRepository.findById(targetReplyId)).willReturn(Optional.of(targetReply));

        // 좋아요 확인 설정
        given(likeReplyRepository.findByMemberAndReply(reqMember, targetReply)).willReturn(Optional.of(replyLike));

        // 좋아요 엔티티 삭제 동작 설정
        doNothing().when(likeReplyRepository).delete(any(ReplyLike.class));

        // when
        LikeAction likeAction = replyLikeService.likeReply(reqMember, targetReplyId);

        // then
        Assertions.assertEquals(LikeAction.UNLIKE, likeAction);
        Assertions.assertEquals(0L, targetReply.getLikeCount(), "좋아요 수 감소 확인하기");
        verify(likeReplyRepository, times(1)).delete(any(ReplyLike.class));
        verify(replyRepository, times(1)).findById(targetReplyId);
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
        given(replyRepository.findById(targetReplyId)).willReturn(Optional.of(targetReply));

        // when
        MyException exception = assertThrows(MyException.class, () -> {
            replyLikeService.likeReply(reqMember, targetReplyId);
        });

        // then
        Assertions.assertEquals(MyErrorCode.NOT_LIKE_MY_REPLY, exception.getErrorCode());
        verify(replyRepository, times(1)).findById(targetReplyId);
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
        given(replyRepository.findById(targetReplyId)).willReturn(Optional.empty());

        // when
        MyException exception = assertThrows(MyException.class, () -> {
            replyLikeService.likeReply(reqMember, targetReplyId);
        });

        // then
        Assertions.assertEquals(MyErrorCode.REPLY_NOT_FOUND, exception.getErrorCode());
        verify(replyRepository, times(1)).findById(targetReplyId);
        verifyNoInteractions(likeReplyRepository);
    }

    // 리플렉션으로 객체에 아이디값 주입
    private void setId(Object target, Long id) {
        try {
            Field idField = target.getClass().getDeclaredField("id");
            boolean isAccessible = idField.isAccessible();
            idField.setAccessible(true);
            idField.set(target, id);
            idField.setAccessible(isAccessible); // 원래 상태로 복구
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("ID 설정 중 오류 발생", e);
        }
    }

    // 리플렉션으로 객체(상속)에 생성, 수정 시간 주입
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = getFieldFromClass(target.getClass(), fieldName); // 필드 가져오기
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true); // 접근 제한 해제
            field.set(target, value); // 값 세팅
            field.setAccessible(isAccessible); // 다시 원래 접근 권한으로 세팅
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(fieldName + " 설정 중 오류 발생", e);
        }
    }

    private Field getFieldFromClass(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) { // 바로 리턴 혹은 상위 클래스에서 clazz를 채우고 다시 try문에서 리턴
            try {
                return clazz.getDeclaredField(fieldName); // 현재 클래스에서 이름이 일치하는 필드 반환, 없으면 NoSuchFiledException
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // 현재 클래스에 필드가 없으면 상위 클래스에서 필드 검색
            }
        }
        throw new NoSuchFieldException(fieldName + " 필드를 찾을 수 없습니다.");
    }
}
