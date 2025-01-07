package kr.inuappcenterportal.inuportal.repository;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.repository.MemberRepository;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.replylike.model.ReplyLike;
import kr.inuappcenterportal.inuportal.domain.replylike.repository.ReplyLikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReplyLikeRepositoryTest {

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private ReplyLikeRepository replyLikeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    private Post post;
    private Member postMember;
    @BeforeEach
    void setUp(){
        postMember = Member.builder().nickname("게시글 작성자 멤버").studentId("11111111").build();
        memberRepository.save(postMember);
        post = Post.builder().title("게시글 제목").content("게시글 내용").category("수강신청").member(postMember).build();
        postRepository.save(post);
    }

    @Test
    @DisplayName("멤버가 좋아요를 누른 댓글인지 확인, 댓글이 조회되면 좋아요를 누른 상태")
    void findByMemberAndReplyTest1(){
        // given
        // Member, reply, replyLike 객체 생성
        Member member = Member.builder().studentId("20202020").nickname("요청자 멤버").build();
        memberRepository.save(member);
        Reply reply = Reply.builder().content("댓글 내용").anonymous(true).build();
        replyRepository.save(reply);
        ReplyLike replyLike = ReplyLike.builder().member(member).reply(reply).build();
        replyLikeRepository.save(replyLike);

        // when
        Optional<ReplyLike> result = replyLikeRepository.findByMemberAndReply(member, reply);

        // then
        assertTrue(result.isPresent(), "멤버가 댓글에 좋아요를 누른 상태.");
    }

    @Test
    @DisplayName("멤버가 좋아요를 누른 댓글인지 확인, 댓글이 조회되지 않으면 좋아요를 누르지 않은 상태")
    void findByMemberAndReplySuccessTest2(){
        // given
        // Member, reply, replyLike 객체 생성
        Member member = Member.builder().studentId("20202020").nickname("요청자 멤버").build();
        memberRepository.save(member);
        Reply reply = Reply.builder().content("댓글 내용").anonymous(true).build();
        replyRepository.save(reply);

        // when
        Optional<ReplyLike> replyLike = replyLikeRepository.findByMemberAndReply(member, reply);

        // then
        assertTrue(replyLike.isEmpty(), "멤버가 댓글에 좋아요를 누른적 없는 상황.");
    }

    @Test
    @DisplayName("댓글 중 멤버가 좋아요를 누른 댓글을 반환")
    void findLikedReplyIdsByMemberTest1(){
        // given
        // Member, reply, replyLike 객체 생성
        Member member = Member.builder().studentId("20202020").nickname("요청자 멤버").build();
        memberRepository.save(member);
        Reply reply1 = Reply.builder().content("댓글1 내용").anonymous(true).build();
        Reply reply2 = Reply.builder().content("댓글2 내용").anonymous(true).build();
        Reply reply3 = Reply.builder().content("댓글3 내용").anonymous(true).build();
        replyRepository.saveAll(List.of(reply1,reply2,reply3));
        ReplyLike replyLike1 = ReplyLike.builder().member(member).reply(reply1).build();
        ReplyLike replyLike2 = ReplyLike.builder().member(member).reply(reply3).build();
        replyLikeRepository.saveAll(List.of(replyLike1, replyLike2));

        // 저장한 댓글들 id 리스트
        List<Long> replyIds = List.of(reply1.getId(), reply2.getId(), reply3.getId());

        // when
        List<Long> likedReplyIds = replyLikeRepository.findLikedReplyIdsByMember(member, replyIds);

        // then
        assertEquals(2,likedReplyIds.size(), "댓글중에서 멤버가 좋아요 누른 댓글은 2개입니다.");
    }

    @Test
    @DisplayName("댓글 중 멤버가 좋아요를 누른 댓글이 없는 경우")
    void findLikedReplyIdsByMemberSuccessTest2(){
        // given
        // Member, reply, replyLike 객체 생성
        Member member = Member.builder().studentId("20202020").nickname("요청자 멤버").build();
        memberRepository.save(member);
        Reply reply1 = Reply.builder().content("댓글1 내용").anonymous(true).build();
        Reply reply2 = Reply.builder().content("댓글2 내용").anonymous(true).build();
        Reply reply3 = Reply.builder().content("댓글3 내용").anonymous(true).build();
        replyRepository.saveAll(List.of(reply1,reply2,reply3));

        // 저장한 댓글들 id 리스트
        List<Long> replyIds = List.of(reply1.getId(), reply2.getId(), reply3.getId());

        // when
        List<Long> likedReplyIds = replyLikeRepository.findLikedReplyIdsByMember(member, replyIds);

        // then
        assertTrue(likedReplyIds.isEmpty(), "댓글중에 멤버가 좋아요 누른 댓글은 없습니다.");
    }

}
