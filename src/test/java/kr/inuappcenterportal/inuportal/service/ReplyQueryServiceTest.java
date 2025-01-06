package kr.inuappcenterportal.inuportal.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReReplyResponseDto;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyListResponseDto;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyResponseDto;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyQueryService;
import kr.inuappcenterportal.inuportal.domain.replylike.repository.LikeReplyRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReplyQueryServiceTest {

    @InjectMocks
    private ReplyQueryService replyQueryService;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private LikeReplyRepository likeReplyRepository;
    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("멤버가 작성한 모든 댓글 조회 - 날짜 정렬 조회(성공)")
    void getReplyByMemberSuccess1Test() {
        // given
        Long reply1Id = 1L;
        Long reply2Id = 2L;
        Long reply1LikeCount = 1L;
        Long reply2LikeCount = 2L;
        Long postId = 1L;
        LocalDate createdDate = LocalDate.now();
        LocalDate modifiedDate = LocalDate.now();
        String sort = "date";

        // 객체 생성 및 설정
        Member member = Member.builder().build();
        Post post = Post.builder().title("게시글 제목").member(member).build();
        setId(post, postId);

        // Reply 설정
        Reply reply1 = Reply.builder().content("댓글1 내용").post(post).build();
        setId(reply1, reply1Id);
        setField(reply1, "likeCount", reply1LikeCount);
        setField(reply1, "createDate", createdDate);
        setField(reply1, "modifiedDate", modifiedDate);

        Reply reply2 = Reply.builder().content("댓글2 내용").post(post).build();
        setId(reply2, reply2Id);
        setField(reply2, "likeCount", reply2LikeCount);
        setField(reply2, "createDate", createdDate);
        setField(reply2, "modifiedDate", modifiedDate);

        given(replyRepository.findAllByMemberAndIsDeletedFalse(member, Sort.by(Sort.Direction.DESC, "id")))
                .willReturn(List.of(reply2, reply1));

        // when
        List<ReplyListResponseDto> result = replyQueryService.getReplyByMember(member, sort);

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals(2, result.size(), "댓글 개수 확인"),
                () -> Assertions.assertEquals(reply2Id, result.get(0).getId(), "첫 번째 댓글 ID 확인"),
                () -> Assertions.assertEquals(reply1Id, result.get(1).getId(), "두 번째 댓글 ID 확인"),
                () -> Assertions.assertEquals("댓글2 내용", result.get(0).getContent(), "첫 번째 댓글 내용 확인"),
                () -> Assertions.assertEquals("댓글1 내용", result.get(1).getContent(), "두 번째 댓글 내용 확인"),
                () -> Assertions.assertEquals("게시글 제목", result.get(0).getTitle(), "첫 번째 댓글 게시글 제목 확인"),
                () -> Assertions.assertEquals("게시글 제목", result.get(1).getTitle(), "두 번째 댓글 게시글 제목 확인"),
                () -> Assertions.assertTrue(result.get(0).getId() > result.get(1).getId(), "ID가 날짜 순으로 정렬되었는지 확인")
        );
        verify(replyRepository, times(1)).findAllByMemberAndIsDeletedFalse(member, Sort.by(Sort.Direction.DESC, "id"));
    }

    @Test
    @DisplayName("멤버가 작성한 모든 댓글 조회 - 좋아요 정렬 조회(성공)")
    void getReplyByMemberSuccess2Test() {
        // given
        Long reply1Id = 1L;
        Long reply2Id = 2L;
        Long reply1LikeCount = 10L;
        Long reply2LikeCount = 20L;
        Long postId = 1L;
        LocalDate createdDate = LocalDate.now();
        LocalDate modifiedDate = LocalDate.now();
        String sort = "like";

        // 객체 생성 및 설정
        Member member = Member.builder().build();
        Post post = Post.builder().title("게시글 제목").member(member).build();
        setId(post, postId);

        // Reply 설정
        Reply reply1 = Reply.builder().content("댓글1 내용").post(post).build();
        setId(reply1, reply1Id);
        setField(reply1, "likeCount", reply1LikeCount);
        setField(reply1, "createDate", createdDate);
        setField(reply1, "modifiedDate", modifiedDate);

        Reply reply2 = Reply.builder().content("댓글2 내용").post(post).build();
        setId(reply2, reply2Id);
        setField(reply2, "likeCount", reply2LikeCount);
        setField(reply2, "createDate", createdDate);
        setField(reply2, "modifiedDate", modifiedDate);

        given(replyRepository.findAllByMemberAndIsDeletedFalse(member, Sort.by(Sort.Direction.DESC, "likeCount", "id")))
                .willReturn(List.of(reply2, reply1));

        // when
        List<ReplyListResponseDto> result = replyQueryService.getReplyByMember(member, sort);

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals(2, result.size(), "댓글 개수 확인"),
                () -> Assertions.assertEquals(reply2LikeCount, result.get(0).getLike(), "첫 번째 댓글 좋아요 수 확인"),
                () -> Assertions.assertEquals(reply1LikeCount, result.get(1).getLike(), "두 번째 댓글 좋아요 수 확인"),
                () -> Assertions.assertEquals("댓글2 내용", result.get(0).getContent(), "첫 번째 댓글 내용 확인"),
                () -> Assertions.assertEquals("댓글1 내용", result.get(1).getContent(), "두 번째 댓글 내용 확인"),
                () -> Assertions.assertEquals("게시글 제목", result.get(0).getTitle(), "첫 번째 댓글 게시글 제목 확인"),
                () -> Assertions.assertEquals("게시글 제목", result.get(1).getTitle(), "두 번째 댓글 게시글 제목 확인"),
                () -> Assertions.assertTrue(result.get(0).getId() > result.get(1).getId(), "ID가 날짜 순으로 정렬되었는지 확인")
        );
        verify(replyRepository, times(1)).findAllByMemberAndIsDeletedFalse(member, Sort.by(Sort.Direction.DESC, "likeCount", "id"));
    }
    @Test
    @DisplayName("멤버가 작성한 모든 댓글 조회 - 잘못된 정렬로 조회(실패)")
    void getReplyByMemberFailTest(){
        // given
        String sort = "잘못된 정렬 타입";

        // Mock 객체 생성 및 설정
        Member member = Member.builder().build();

        // when
        MyException exception = assertThrows(MyException.class, () -> {
            replyQueryService.getReplyByMember(member, sort);
        });

        // then
        Assertions.assertEquals(MyErrorCode.WRONG_SORT_TYPE, exception.getErrorCode());
        verify(replyRepository, times(0)).findAllByMemberAndIsDeletedFalse(member, Sort.by(Sort.Direction.DESC, "id"));
        verify(replyRepository, times(0)).findAllByMemberAndIsDeletedFalse(member, Sort.by(Sort.Direction.DESC, "likeCount", "id"));
    }

    // mock 객체로는 너무 어려운듯?
    @Test
    @DisplayName("게시글에 해당하는 댓글 조회 - 게시글에 해당하는 댓글 모두 조회(성공)")
    void getRepliesSuccessTest(){
        // given
        Long postId = 1L;
        Long postMemberId = 1L;
        Long reply1MemberId = 2L;
        Long reReplyMemberId = 3L;
        Long reqMemberId = 4L;
        Long reply1Id = 1L;
        Long reply2Id = 2L;
        Long reReplyId = 3L;
        LocalDate createDate = LocalDate.now();
        LocalDate modifiedDate = LocalDate.now();

        Member postMember = Member.builder().build();
        setId(postMember,postMemberId);
        Member reply1Member = Member.builder().build();
        setId(reply1Member, reply1MemberId);
        Member reReplyMember = Member.builder().nickname("대댓글 멤버").build();
        setId(reReplyMember, reReplyMemberId);
        Member reqMember = Member.builder().build();
        setId(reqMember, reqMemberId);

        Post post = Post.builder().member(postMember).build();
        setId(post, postId);

        Reply reply1 = Reply.builder()
                .post(post)
                .anonymous(true)
                .member(reply1Member)
                .number(1L)
                .build();
        setId(reply1, reply1Id);
        setField(reply1,"createDate",createDate);
        setField(reply1,"modifiedDate",modifiedDate);

        Reply reply2 = Reply.builder()
                .post(post)
                .anonymous(true)
                .number(2L)
                .build();
        setId(reply2, reply2Id);
        setField(reply2,"createDate",createDate);
        setField(reply2,"modifiedDate",modifiedDate);

        Reply reReply = Reply.builder()
                .post(post)
                .reply(reply1)
                .anonymous(false)
                .member(reReplyMember)
                .build();
        setId(reReply, reReplyId);
        setField(reReply,"createDate",createDate);
        setField(reReply,"modifiedDate",modifiedDate);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(replyRepository.findAllNonDeletedOrHavingChildren(post))
                .thenReturn(List.of(reply1, reply2, reReply));
        when(likeReplyRepository.findLikedReplyIdsByMember(eq(reqMember), anyList()))
                .thenReturn(List.of(reply1Id, reReplyId));

        // when
        List<ReplyResponseDto> result = replyQueryService.getRepliesByPost(postId, reqMember);

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals(2, result.size(), "부모 댓글 개수 확인"),
                () -> Assertions.assertEquals(reply1Id, result.get(0).getId(), "첫 번째 댓글 ID 확인"),
                () -> Assertions.assertTrue(result.get(0).getIsLiked(), "첫 번째 댓글에 좋아요 여부 확인"),
                () -> Assertions.assertEquals(reReplyId, result.get(0).getReReplies().get(0).getId(), "첫 번째 댓글의 대댓글 ID 확인"),
                () -> Assertions.assertEquals("횃불이1", result.get(0).getWriter(), "첫 번째 댓글의 익명 이름 확인"),
                () -> Assertions.assertEquals(reply2Id, result.get(1).getId(), "두 번째 댓글 ID 확인"),
                () -> Assertions.assertEquals("(알수없음)", result.get(1).getWriter(), "두 번째 댓글의 삭제된 이름 확인"),
                () -> Assertions.assertFalse(result.get(1).getIsLiked(), "두 번째 댓글에 좋아요 여부 확인")
        );

        verify(postRepository, times(1)).findById(postId);
        verify(replyRepository, times(1)).findAllNonDeletedOrHavingChildren(post);
        verify(likeReplyRepository, times(1)).findLikedReplyIdsByMember(eq(reqMember), anyList());
    }
    @Test
    @DisplayName("게시글에 해당하는 댓글 조회 - 없는 게시글 아이디(실패)")
    void getRepliesFailTest(){
        // given
        Long postId = 100L; // 존재하지 않는 게시글 ID
        Long memberId = 1L;

        Member member = Member.builder().build();
        setId(member, memberId);

        when(postRepository.findById(postId)).thenReturn(Optional.empty()); // 게시글을 찾을 수 없도록 설정

        // when & then
        MyException exception = Assertions.assertThrows(MyException.class, () -> {
            replyQueryService.getRepliesByPost(postId, member);
        });

        Assertions.assertEquals(MyErrorCode.POST_NOT_FOUND, exception.getErrorCode(), "예외 코드 확인");
        verify(postRepository, times(1)).findById(postId);
        verify(replyRepository, never()).findAllNonDeletedOrHavingChildren(any());
        verify(likeReplyRepository, never()).findLikedReplyIdsByMember(any(), anyList());
    }

    @Test
    @DisplayName("베스트 댓글 조회 - 좋아요 5개 이상 댓글을 조회합니다.(성공)")
    void getBestRepliesSuccessTest() {
        // given
        Long postId = 1L;
        Long reply1MemberId = 1L;
        Long reReplyMemberId = 3L;
        Long reqMemberId = 2L;
        Long reply1Id = 1L;
        Long reply2Id = 2L;
        Long reReplyId = 3L;
        LocalDate createDate = LocalDate.now();
        LocalDate modifiedDate = LocalDate.now();

        Member reply1Member = Member.builder().build();
        setId(reply1Member, reply1MemberId);

        Member reReplyMember = Member.builder().nickname("대댓글 작성자").build();
        setId(reReplyMember, reReplyMemberId);

        Member reqMember = Member.builder().build();
        setId(reqMember, reqMemberId);

        Post post = Post.builder().build();
        setId(post, postId);

        Reply reply1 = Reply.builder()
                .post(post)
                .anonymous(true)
                .number(1L)
                .member(reply1Member)
                .build();
        setId(reply1, reply1Id);
        setField(reply1,"likeCount",10L);
        setField(reply1,"createDate",createDate);
        setField(reply1,"modifiedDate",modifiedDate);

        Reply reply2 = Reply.builder()
                .post(post)
                .anonymous(true)
                .build();
        setId(reply2, reply2Id);
        setField(reply2,"likeCount",4L);
        setField(reply2,"createDate",createDate);
        setField(reply2,"modifiedDate",modifiedDate);

        Reply reReply = Reply.builder()
                .post(post)
                .anonymous(false)
                .member(reReplyMember)
                .build();
        setId(reReply, reReplyId);
        setField(reReply,"likeCount",5L);
        setField(reReply,"createDate",createDate);
        setField(reReply,"modifiedDate",modifiedDate);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(replyRepository.findBestReplies(post)).thenReturn(List.of(reply1,reReply));
        when(likeReplyRepository.findLikedReplyIdsByMember(eq(reqMember), anyList()))
                .thenReturn(List.of(reply1Id));

        // when
        List<ReReplyResponseDto> result = replyQueryService.getBestReplies(postId, reqMember);

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals(2, result.size(), "베스트 댓글 개수 확인"),
                () -> Assertions.assertEquals(reply1Id, result.get(0).getId(), "첫 번째 베스트 댓글 ID 확인"),
                () -> Assertions.assertTrue(result.get(0).getIsLiked(), "첫 번째 베스트 댓글 좋아요 여부 확인"),
                () -> Assertions.assertEquals("횃불이1", result.get(0).getWriter(), "첫번째 베스트 댓글 이름 확인"),
                () -> Assertions.assertEquals(reReplyId, result.get(1).getId(), "두 번째 베스트 댓글 ID 확인"),
                () -> Assertions.assertFalse(result.get(1).getIsLiked(), "두 번째 베스트 댓글 좋아요 여부 확인"),
                () -> Assertions.assertEquals("대댓글 작성자", result.get(1).getWriter(), "두번째 베스트 댓글 이름 확인")
        );
        verify(postRepository, times(1)).findById(postId);
        verify(replyRepository, times(1)).findBestReplies(post);
        verify(likeReplyRepository, times(1)).findLikedReplyIdsByMember(eq(reqMember), anyList());
    }
    @Test
    @DisplayName("베스트 댓글 조회 - 없는 게시글(실패)")
    void getBestRepliesFailTest() {
        // given
        Long postId = 1L;

        // 객체 생성
        Member reqMember = Member.builder().build();

        // Post 설정
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when
        MyException exception = Assertions.assertThrows(MyException.class, () -> {
            replyQueryService.getBestReplies(postId, reqMember);
        });

        // then
        Assertions.assertEquals(MyErrorCode.POST_NOT_FOUND, exception.getErrorCode());
        verify(postRepository, times(1)).findById(postId);
        verify(replyRepository, times(0)).findBestReplies(any(Post.class));
        verify(likeReplyRepository, times(0)).findLikedReplyIdsByMember(eq(reqMember), anyList());
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
