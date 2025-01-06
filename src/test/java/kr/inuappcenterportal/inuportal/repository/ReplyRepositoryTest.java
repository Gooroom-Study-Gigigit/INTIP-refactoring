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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/init_test.sql") // SQL 파일 경로
public class ReplyRepositoryTest {
    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void Sql_파일실행테스트(){
        Member member = memberRepository.findById(1L).orElseThrow();
        System.out.println("member 1의 id : " + member.getId());
        Assertions.assertNotNull(member);
    }

    @Test
    @DisplayName("유저의 모든 댓글을 생성시간 정렬 기준으로 조회합니다. (성공)")
    void findAllByMemberAndIsDeletedFalseSuccessTest1(){
        // given
        Long memberId = 1L;
        Member member = memberRepository.findById(memberId).orElseThrow();
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        System.out.println("member 1의 id : " + member.getId());

        //when
        List<Reply> replyList = replyRepository.findAllByMemberAndIsDeletedFalse(member,sort);
        System.out.println("replyList 사이즈: " + replyList.size());

        //then
        assertFalse(replyList.isEmpty(), "댓글 리스트가 비어있지 않습니다.");

        // 댓글 삭제 여부 검증
        replyList.forEach(reply -> {
            assertFalse(reply.getIsDeleted());
        });

        // 생성 시간 내림차순으로 조회 되었는지 검증
        for (int i = 1; i < replyList.size(); i++) {
            System.out.println(replyList.get(i).getId());
            assertTrue(
                    replyList.get(i - 1).getId() >= replyList.get(i).getId(),
                    "댓글 리스트가 id 내림차순으로 정렬되어 있습니다."
            );
        }
    }
    @Test
    @DisplayName("유저의 모든 댓글을 좋아요 개수 내림차순 정렬 기준으로 조회합니다.(성공)")
    void findAllByMemberAndIsDeletedFalseSuccessTest2(){
        // given
        Long memberId = 1L;
        Member member = memberRepository.findById(memberId).orElseThrow();
        Sort sort = Sort.by(Sort.Direction.DESC, "likeCount", "id");
        System.out.println("member 1의 id : " + member.getId());

        //when
        List<Reply> replyList = replyRepository.findAllByMemberAndIsDeletedFalse(member,sort);
        System.out.println("replyList 사이즈: " + replyList.size());

        //then
        assertFalse(replyList.isEmpty(), "댓글 리스트가 비어있지 않습니다.");
        
        // 댓글 삭제 여부 검증
        replyList.forEach(reply -> {
            assertFalse(reply.getIsDeleted());
        });

        // 좋아요 개수 내림차순으로 조회 되었는지 검증
        for (int i = 1; i < replyList.size(); i++) {
            System.out.println(replyList.get(i).getId());
            assertTrue(
                    replyList.get(i - 1).getLikeCount() >= replyList.get(i).getLikeCount(),
                    "댓글 리스트가 id 내림차순으로 정렬되어 있습니다."
            );
        }
    }
    @Test
    @DisplayName("유저의 모든 댓글을 생성시간 정렬 기준으로 조회합니다. (댓글이 없는 경우)")
    void findAllByMemberAndIsDeletedFalseSuccessTest3(){
        // given
        Long nonReplyMemberId = 10L;
        Member member = memberRepository.findById(nonReplyMemberId).orElseThrow();
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        System.out.println("member 1의 id : " + member.getId());

        //when
        List<Reply> replyList = replyRepository.findAllByMemberAndIsDeletedFalse(member,sort);
        System.out.println("replyList 사이즈: " + replyList.size());

        //then
        assertTrue(replyList.isEmpty(), "댓글 리스트가 비어있습니다.");
    }
    @Test
    @DisplayName("유저가 게시글에 작성한 첫 번째 댓글 가져오기(성공)")
    void findFirstByPostAndMemberSuccessTest1(){
        // given
        Long memberId = 1L;
        Long postId = 1L;
        Member member = memberRepository.findById(memberId).orElseThrow();
        Post post = postRepository.findById(postId).orElseThrow();
        System.out.println("member 1의 id : " + member.getId());

        //when
        Optional<Reply> reply = replyRepository.findFirstByPostAndMember(post, member);
        System.out.println("reply id : " + reply.get().getId());

        //then
        assertFalse(reply.isEmpty(), "댓글이 empty 가 아닙니다.");
        assertTrue(reply.get().getNumber() > 0,"number 가 0보다 큽니다.");
    }
    @Test
    @DisplayName("유저가 게시글에 작성한 첫 번째 댓글 가져오기 (댓글 없음 테스트)")
    void findFirstByPostAndMemberSuccessTest2() {
        // given
        Long memberId = 10L;
        Long postId = 1L;
        Member member = memberRepository.findById(memberId).orElseThrow();
        Post post = postRepository.findById(postId).orElseThrow();
        System.out.println("member 10의 id : " + member.getId());

        //when
        Optional<Reply> reply = replyRepository.findFirstByPostAndMember(post, member);

        //then
        assertTrue(reply.isEmpty(), "댓글이 empty 입니다.");
    }
    @Test
    @DisplayName("게시글에 댓글을 조회합니다. 삭제 된 댓글이 아니거나 or 삭제 된 부모 댓글이지만 자식 댓글이 있는 경우(삭제되지 않은) (성공)")
    void findAllNonDeletedOrHavingChildrenSuccessTest1(){
        // given
        Long postId = 1L;
        Post post = postRepository.findById(postId).orElseThrow();
        System.out.println("postId  : " + post.getId());

        //when
        List<Reply> replyList = replyRepository.findAllNonDeletedOrHavingChildren(post);
        System.out.println("reply 개수 : " + replyList.size());

        //then
        int nonDeleteCount = 0;
        int deleteWithChildrenCount = 0;

        for(Reply reply: replyList){
            if(!reply.getIsDeleted()){
                nonDeleteCount++;
            }else{
                boolean hasNonDeletedChild = replyList.stream()
                        .anyMatch(child -> child.getReply() != null
                                && child.getReply().getId().equals(reply.getId())
                                && !child.getIsDeleted());
                assertTrue(
                        hasNonDeletedChild, "삭제된 댓글은 삭제되지 않은 대댓글을 가지고 있습니다."
                );
                if(hasNonDeletedChild){
                    deleteWithChildrenCount++;
                }
            }
        }
        assertEquals(21, replyList.size(), "조회된 댓글의 총 개수는 21개여야 합니다.");
        assertEquals(20, nonDeleteCount, "isDelete = false 인 댓글의 개수는 20개 입니다.");
        assertEquals(1, deleteWithChildrenCount,"isDelete = true 이지만, 삭제되지 않은 대댓글을 가진 댓글의 개수는 1개 입니다. ");
    }
    @Test
    @DisplayName("게시글에 댓글을 조회합니다. (댓글이 없는 경우)")
    void findAllNonDeletedOrHavingChildrenSuccessTest2(){
        // given
        Long nonReplyPostId = 2L;
        Post post = postRepository.findById(nonReplyPostId).orElseThrow();
        System.out.println("postId  : " + post.getId());

        //when
        List<Reply> replyList = replyRepository.findAllNonDeletedOrHavingChildren(post);
        System.out.println("reply 개수 : " + replyList.size());

        //then
        assertTrue(replyList.isEmpty(), "게시글에 댓글이 없습니다.");
    }
    @Test
    @DisplayName("게시글에 좋아요가 5 이상인 댓글을 좋아요 수 기준으로 내림차순 정렬하여 최대 3개 조회. (성공)")
    void findBestRepliesSuccessTest(){
        // given
        Long postId = 1L;
        Post post = postRepository.findById(postId).orElseThrow();
        System.out.println("postId : " + post.getId());

        // when
        List<Reply> bestReplies = replyRepository.findBestReplies(post);
        System.out.println("bestReplies 개수 : " + bestReplies.size());

        for(int i=0; i<bestReplies.size(); i++){
            System.out.println("bestReplyId : " + bestReplies.get(i).getId());
        }

        // then
        assertTrue(bestReplies.size() == 3, "베스트 댓글은 최대 3개 조회합니다");
        for(int i=1; i<bestReplies.size(); i++){
            assertTrue(bestReplies.get(i-1).getLikeCount() > bestReplies.get(i).getLikeCount()
                    || bestReplies.get(i-1).getId() >= bestReplies.get(i).getId());
        }
    }
    @Test
    @DisplayName("게시글에 좋아요가 5 이상인 댓글을 좋아요 수 기준으로 내림차순 정렬하여 최대 3개 조회. (해당하는 댓글 없음)")
    void findBestRepliesSuccessTest2(){
        // given
        Long nonReplyPostId = 2L;
        Post post = postRepository.findById(2L).orElseThrow();
        System.out.println("postId : " + post.getId());

        // when
        List<Reply> bestReplies = replyRepository.findBestReplies(post);
        System.out.println("bestReplies 개수 : " + bestReplies.size());

        for(int i=0; i<bestReplies.size(); i++){
            System.out.println("bestReplyId : " + bestReplies.get(i).getId());
        }

        // then
        assertTrue(bestReplies.isEmpty(), "게시글에 좋아요 5개 이상인 댓글이 없습니다.");
    }
    @Test
    @DisplayName(" Pessimistic Lock을 사용하여 Reply를 조회합니다. (성공)")
    void findByIdWithLockSuccessTest(){
        // given
        Long replyId = 1L;

        // when
        Reply reply = replyRepository.findByIdWithLock(replyId).orElseThrow();
        System.out.println("replyId : " + reply.getId());

        // then
        assertEquals(replyId, reply.getId(),"조회한 댓글의 id는 1L 입니다.");
        LockModeType lockMode = entityManager.getLockMode(reply);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, lockMode, "Pessimistic Lock이 설정되어야 합니다.");
    }
}
