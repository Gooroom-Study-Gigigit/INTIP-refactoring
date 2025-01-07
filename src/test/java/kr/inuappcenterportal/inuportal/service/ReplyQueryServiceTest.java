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
import kr.inuappcenterportal.inuportal.domain.replylike.repository.ReplyLikeRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import kr.inuappcenterportal.inuportal.util.TestReflectionUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

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
    private ReplyLikeRepository likeReplyRepository;
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
        Long replyNumber = 0L;
        Long postId = 1L;
        LocalDate createdDate = LocalDate.now();
        LocalDate modifiedDate = LocalDate.now();
        String sort = "date";

        // Member, Post, Reply 객체 생성
        Member member = Member.builder().build();
        Post post = createPost(postId,member);
        Reply reply1 = createReply(reply1Id, post, member, "댓글1 내용",reply1LikeCount,createdDate, modifiedDate, true, null,replyNumber);
        Reply reply2 = createReply(reply2Id, post, member, "댓글2 내용",reply2LikeCount,createdDate, modifiedDate, true, null,replyNumber);

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
                () -> Assertions.assertEquals(postId, result.get(0).getPostId(), "첫 번째 댓글 게시글 ID 확인"),
                () -> Assertions.assertEquals(postId, result.get(1).getPostId(), "두 번째 댓글 게시글 ID 확인"),
                () -> Assertions.assertTrue(isSortedByDate(result.get(0).getId(),result.get(1).getId()), "ID가 날짜 순으로 정렬되었는지 확인")
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
        Long replyNumber = 0L;
        Long postId = 1L;
        LocalDate createdDate = LocalDate.now();
        LocalDate modifiedDate = LocalDate.now();
        String sort = "like";

        // Member, Post, Reply 객체 생성
        Member member = Member.builder().build();
        Post post = createPost(postId,member);
        Reply reply1 = createReply(reply1Id, post, member, "댓글1 내용",reply1LikeCount,createdDate, modifiedDate, true, null,replyNumber);
        Reply reply2 = createReply(reply2Id, post, member, "댓글2 내용",reply2LikeCount,createdDate, modifiedDate, true, null,replyNumber);

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
                () -> Assertions.assertEquals(postId, result.get(0).getPostId(), "첫 번째 댓글 게시글 ID 확인"),
                () -> Assertions.assertEquals(postId, result.get(1).getPostId(), "두 번째 댓글 게시글 ID 확인"),
                () -> Assertions.assertTrue(result.get(0).getId() > result.get(1).getId(), "ID가 날짜 순으로 정렬되었는지 확인")
        );
        verify(replyRepository, times(1)).findAllByMemberAndIsDeletedFalse(member, Sort.by(Sort.Direction.DESC, "likeCount", "id"));
    }
    @Test
    @DisplayName("멤버가 작성한 모든 댓글 조회 - 잘못된 정렬 타입으로 조회(실패)")
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
        Long reply1Number = 1L;
        Long reply2Number = 2L;
        LocalDate createDate = LocalDate.now();
        LocalDate modifiedDate = LocalDate.now();

        // Member, Post, Reply 객체 생성
        Member postMember = createMember(postMemberId,"게시글 작성자 멤버");
        Member reply1Member = createMember(reply1MemberId,"댓글 1 작성자 멤버");
        Member reReplyMember = createMember(reReplyMemberId, "대댓글 작성자 멤버");
        Member reqMember = createMember(reqMemberId, "요청자 멤버");
        Post post = createPost(postId, postMember);
        Reply reply1 = createReply(reply1Id, post, reply1Member, "댓글1 내용",0L,createDate, modifiedDate, true, null,reply1Number);
        Reply reply2 = createReply(reply2Id, post, null, "댓글2 내용",0L,createDate, modifiedDate, false, null,reply2Number);
        Reply reReply = createReply(reReplyId, post, reReplyMember, "대댓글 내용",0L,createDate, modifiedDate, false, reply1,0L);

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

        // Member 객체 생성
        Member member = createMember(memberId,"요청자 멤버");

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
        Long reReplyId = 3L;
        Long reply1LikeCount = 10L;
        Long reReplyLikeCount = 5L;
        LocalDate createDate = LocalDate.now();
        LocalDate modifiedDate = LocalDate.now();

        // Member, Post, Reply 객체 생성
        Member reply1Member = createMember(reply1MemberId,"댓글1 작성자 멤버");
        Member reReplyMember = createMember(reReplyMemberId,"대댓글 작성자 멤버");
        Member reqMember = createMember(reqMemberId, "요청자 멤버");
        Post post = createPost(postId,Member.builder().build());
        Reply reply1 = createReply(reply1Id, post, reply1Member, "댓글1 내용",reply1LikeCount,createDate, modifiedDate, true, null,1L);
        Reply reReply = createReply(reReplyId, post, reReplyMember, "대댓글 내용",reReplyLikeCount,createDate, modifiedDate, false, reply1,0L);

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
                () -> Assertions.assertEquals("대댓글 작성자 멤버", result.get(1).getWriter(), "두번째 베스트 댓글 이름 확인")
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

    // Member 객체 생성 메서드
    public static Member createMember(Long id, String nickname) {
        Member member = Member.builder().nickname(nickname).build();
        TestReflectionUtil.setId(member, id);
        return member;
    }

    // Post 객체 생성 메서드
    public static Post createPost(Long id, Member member) {
        Post post = Post.builder().member(member).build();
        TestReflectionUtil.setId(post, id);
        return post;
    }

    // Reply 객체 생성 메서드
    public static Reply createReply(Long id, Post post, Member member, String content, Long likeCount,
                                    LocalDate createDate, LocalDate modifiedDate, boolean isAnonymous, Reply parentReply, Long number) {
        Reply reply = Reply.builder()
                .post(post)
                .member(member)
                .number(number)
                .content(content)
                .anonymous(isAnonymous)
                .reply(parentReply)
                .build();
        TestReflectionUtil.setId(reply, id);
        if (likeCount != null) TestReflectionUtil.setField(reply, "likeCount", likeCount);
        if (createDate != null) TestReflectionUtil.setField(reply, "createDate", createDate);
        if (modifiedDate != null) TestReflectionUtil.setField(reply, "modifiedDate", modifiedDate);

        return reply;
    }

    private boolean isSortedByDate(Long reply1Id, Long reply2Id) {
        return reply1Id > reply2Id;
    }
}
