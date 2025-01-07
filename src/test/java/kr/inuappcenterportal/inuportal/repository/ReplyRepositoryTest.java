package kr.inuappcenterportal.inuportal.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.repository.MemberRepository;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReplyRepositoryTest {
    @Autowired
    private ReplyRepository replyRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;
    @PersistenceContext
    private EntityManager entityManager;

    private Member postMember;
    private Member reqMember;
    private Post post;
    @BeforeEach
    void setUp(){
        reqMember = Member.builder().studentId("20202020").nickname("홍길동").build();
        memberRepository.save(reqMember);

        postMember = Member.builder().nickname("게시글 작성자 멤버").studentId("11111111").build();
        memberRepository.save(postMember);

        post = Post.builder().title("게시글 제목").content("게시글 내용").category("수강신청").member(postMember).build();
        postRepository.save(post);
    }

    @AfterEach
    void clearData() {
        replyRepository.deleteAll();
        postRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("멤버가 작성한 모든 댓글을 생성시간 기준 내림차순으로 조회.")
    void findAllByMemberAndIsDeletedFalseTest1(){
        // given
        List<Reply> memberReplies = Arrays.asList(
                createReply(reqMember,null,"댓글1 내용",true,null,post),
                createReply(reqMember, null,"댓글2 내용",true,null,post),
                createReply(reqMember, null,"댓글3 내용",true,null,post));
        replyRepository.saveAll(memberReplies);
        // 정렬 기준 생성
        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        //when
        List<Reply> replyList = replyRepository.findAllByMemberAndIsDeletedFalse(reqMember,sort);

        //then
        assertFalse(replyList.isEmpty(), "댓글 리스트가 비어있지 않습니다.");

        // 댓글 삭제 여부 검증
        replyList.forEach(reply -> {
            assertFalse(reply.getIsDeleted());
        });

        // 생성 시간 내림차순으로 조회 되었는지 검증
        assertTrue(isSortedByDate(replyList));
    }

    @Test
    @DisplayName("멤버가 작성한 모든 댓글을 좋아요 개수 기준 내림차순으로 조회," +
            "만약 좋아요 개수가 같은 경우 id 기준 내림차순 조회.")
    void findAllByMemberAndIsDeletedFalseTest2(){
        // given
        List<Reply> memberReplies = Arrays.asList(
                createReply(reqMember,null,"댓글1 내용",true,5L,post),
                createReply(reqMember, null,"댓글2 내용",true,10L,post),
                createReply(reqMember, null,"댓글3 내용",true,15L,post));
        replyRepository.saveAll(memberReplies);

        // 정렬 기준 생성
        Sort sort = Sort.by(Sort.Direction.DESC, "likeCount","id");

        //when
        List<Reply> replyList = replyRepository.findAllByMemberAndIsDeletedFalse(reqMember,sort);

        //then
        assertFalse(replyList.isEmpty(), "댓글 리스트가 비어있지 않습니다.");
        
        // 댓글 삭제 여부 검증
        replyList.forEach(reply -> {
            assertFalse(reply.getIsDeleted());
        });

        // 좋아요 개수 내림차순으로 조회 되었는지 검증, 만약 좋아요 개수가 같은 경우 id값 기준 내림차순으로 조회 되었는지 검증
        assertTrue(isSortedByLikeAndDate(replyList));
    }
    @Test
    @DisplayName("멤버의 모든 댓글을 생성시간 기준 조회 할 때, 댓글이 없는 경우 빈 리스트 반환")
    void findAllByMemberAndIsDeletedFalseTest3(){
        // given
        // 정렬 기준 생성
        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        //when
        List<Reply> replyList = replyRepository.findAllByMemberAndIsDeletedFalse(reqMember,sort);

        //then
        assertTrue(replyList.isEmpty(), "댓글 리스트가 비어있습니다.");
    }
    @Test
    @DisplayName("멤버가 게시글에 작성한 첫 번째 댓글 조회.")
    void findFirstByPostAndMemberTest1(){
        // given
        Reply memberFirstReply = createReply(reqMember,null,"멤버가 게시글에 첫 번째 작성한 댓글",true,null,post);
        Reply memberSecondReply = createReply(reqMember,null,"멤버가 게시글에 두 번째 작성한 댓글",true,null,post);
        replyRepository.saveAll(List.of(memberFirstReply, memberSecondReply));
        //when
        Optional<Reply> reply = replyRepository.findFirstByPostAndMember(post, reqMember);

        //then
        assertFalse(reply.isEmpty(), "댓글이 empty 가 아닙니다.");
        assertEquals("멤버가 게시글에 첫 번째 작성한 댓글", reply.get().getContent(),"첫 번째 게시글을 가져왔는지 확인합니다.");
    }
    @Test
    @DisplayName("유저가 게시글에 작성한 댓글이 없는 경우. 빈 값 반환")
    void findFirstByPostAndMemberTest2() {
        // given

        //when
        Optional<Reply> reply = replyRepository.findFirstByPostAndMember(post, reqMember);

        //then
        assertTrue(reply.isEmpty(), "댓글이 empty 입니다.");
    }
    @Test
    @DisplayName("게시글에 댓글들을 조회 " +
            "삭제 된 댓글이 아니거나 or 삭제 된 부모 댓글이지만 삭제되지 않은 자식 댓글이 있을 때")
    void findAllNonDeletedOrHavingChildrenTest1(){
        // given
        Reply normalReply = createReply(reqMember,null,"정상 댓글",true,null,post);
        Reply deleteReplyWithoutChildren = createReply(reqMember, null,"삭제된 댓글,자식 없음",true,null,post);
        setField(deleteReplyWithoutChildren,"isDeleted",true);
        Reply deletedReplyWithChildren = createReply(reqMember, null,"삭제된 댓글,자식 있음",true,null,post);
        setField(deletedReplyWithChildren,"isDeleted",true);
        Reply childReply = createReply(reqMember,deletedReplyWithChildren,"정상 대댓글",true,null,post);

        replyRepository.saveAll(List.of(normalReply,deletedReplyWithChildren,deletedReplyWithChildren,childReply));

        //when
        List<Reply> replyList = replyRepository.findAllNonDeletedOrHavingChildren(post);

        //then
        assertEquals(3, replyList.size(), "조회된 댓글의 총 개수는 3개여야 합니다.");
        assertTrue(hasDeletedReplyWithChildren(replyList));
    }
    @Test
    @DisplayName("빈 리스트 반환 +" +
            "게시글에 댓글이 없거나 or 삭제된 댓글만 있거나 or 삭제된 부모 댓글에 삭제되지 않은 자식 댓글이 없는 경우")
    void findAllNonDeletedOrHavingChildrenTest2(){
        // given
        Reply deleteReplyWithoutChildren = createReply(reqMember, null,"삭제된 댓글,자식 없음",true,null,post);
        setField(deleteReplyWithoutChildren,"isDeleted",true);
        replyRepository.save(deleteReplyWithoutChildren);

        //when
        List<Reply> replyList = replyRepository.findAllNonDeletedOrHavingChildren(post);

        //then
        assertTrue(replyList.isEmpty(), "게시글에 댓글이 없어야 합니다.");
    }
    @Test
    @DisplayName("게시글에 좋아요가 5 이상인 댓글을 좋아요 수 기준으로 내림차순 정렬하여 최대 3개 조회.")
    void findBestRepliesTest1(){
        // given
        List<Reply> memberReplies = Arrays.asList(
                createReply(reqMember,null,"댓글1 내용",true,5L,post),
                createReply(reqMember, null,"댓글2 내용",true,10L,post),
                createReply(reqMember, null,"댓글3 내용",true,15L,post),
                createReply(reqMember, null,"댓글4 내용",true,20L,post));
        replyRepository.saveAll(memberReplies);

        // when
        List<Reply> bestReplies = replyRepository.findBestReplies(post);

        // then
        assertEquals(3, bestReplies.size(), "베스트 댓글은 최대 3개 조회합니다");
        assertTrue(isSortedByLikeAndDate(bestReplies));
    }
    @Test
    @DisplayName("게시글에 좋아요가 5 이상인 댓글이 없을 때 빈 리스트 반환")
    void findBestRepliesSuccessTest2(){
        // given
        List<Reply> memberReplies = Arrays.asList(
                createReply(reqMember,null,"댓글1 내용",true,1L,post),
                createReply(reqMember, null,"댓글2 내용",true,2L,post),
                createReply(reqMember, null,"댓글3 내용",true,3L,post),
                createReply(reqMember, null,"댓글4 내용",true,4L,post));
        replyRepository.saveAll(memberReplies);

        // when
        List<Reply> bestReplies = replyRepository.findBestReplies(post);

        // then
        assertTrue(bestReplies.isEmpty(), "게시글에 좋아요 5개 이상인 댓글이 없습니다.");
    }
    @Test
    @DisplayName("Pessimistic Lock 을 사용하여 댓글을 조회")
    void findByIdWithLockSuccessTest(){
        // given
        Reply reply = createReply(reqMember, null,"댓글 내용",true,null,post);
        replyRepository.save(reply);

        // when
        Optional<Reply> result = replyRepository.findByIdWithLock(reply.getId());

        // then
        assertTrue(result.isPresent(),"댓글이 존재합니다.");
        LockModeType lockMode = entityManager.getLockMode(reply);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, lockMode, "Pessimistic Lock이 설정되어야 합니다.");
    }

    // 생성시간 기준 내림차순으로 조회
    private boolean isSortedByDate(List<Reply> replyList){
        for (int i = 1; i < replyList.size(); i++) {
            if(replyList.get(i - 1).getId() < replyList.get(i).getId()) return false;
        }
        return true;
    }
    // 좋아요 개수 기준 내림차순 조회, 만약 좋아요 개수가 같다면 생성시간 기준 내림차순 조회
    private boolean isSortedByLikeAndDate(List<Reply> replyList){
        for (int i = 1; i < replyList.size(); i++) {
            // 좋아요 개수 기준 내림차순 정렬 확인
            if(replyList.get(i - 1).getLikeCount() < replyList.get(i).getLikeCount()){
                return false;
            }

            // 만약 좋아요 개수가 같다면, 생성시간 기준 내림차순 정렬 확인
            if(replyList.get(i-1).getLikeCount() == replyList.get(i).getLikeCount()
                    && replyList.get(i-1).getId() < replyList.get(i).getId()){
                return false;
            }
        }
        return true;
    }
    
    // 댓글 객체 생성 메서드
    private Reply createReply(Member member, Reply parentReply, String content, boolean anonymous, Long likeCount,Post post) {
        Reply reply = Reply.builder()
                .member(member)
                .reply(parentReply)
                .content(content)
                .anonymous(anonymous)
                .post(post)
                .build();
        if(likeCount != null) setField(reply,"likeCount",likeCount);
        return reply;
    }

    // 조회된 댓글중 삭제된 댓글들이 자식 댓글을 가지고 있는지 검증하는 메서드
    public boolean hasDeletedReplyWithChildren(List<Reply> replies) {
        return replies.stream()
                .filter(Reply::getIsDeleted) // 삭제된 댓글만 필터링
                .anyMatch(deletedReply -> replies.stream()
                        .anyMatch(child -> child.getReply() != null
                                && child.getReply().getId().equals(deletedReply.getId())
                                && !child.getIsDeleted())); // 자식 댓글이 존재하며 삭제되지 않은 경우 확인
    }
}
