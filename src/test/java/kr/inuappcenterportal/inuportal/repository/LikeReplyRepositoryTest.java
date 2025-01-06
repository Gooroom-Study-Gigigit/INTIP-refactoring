package kr.inuappcenterportal.inuportal.repository;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.repository.MemberRepository;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.replylike.model.ReplyLike;
import kr.inuappcenterportal.inuportal.domain.replylike.repository.LikeReplyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/init_test.sql") // SQL 파일 경로
public class LikeReplyRepositoryTest {

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private LikeReplyRepository likeReplyRepository;

    @Autowired
    private MemberRepository memberRepository;


    @Test
    @DisplayName("유저가 댓글에 누른 좋아요를 조회합니다. 좋아요 존재 (성공)")
    void findByMemberAndReplySuccessTest1(){
        // given
        Long memberId = 6L;
        Long replyId = 1L;
        Member member = memberRepository.findById(memberId).orElseThrow();
        Reply reply = replyRepository.findById(replyId).orElseThrow();

        // when
        Optional<ReplyLike> replyLike = likeReplyRepository.findByMemberAndReply(member, reply);

        // then
        assertTrue(replyLike.isPresent(), "유저가 댓글에 좋아요를 누른적 있는 상황.");
    }

    @Test
    @DisplayName("유저가 댓글에 누른 좋아요를 조회합니다. 좋아요 존재 x (성공)")
    void findByMemberAndReplySuccessTest2(){
        // given
        Long memberId = 1L;
        Long replyId = 1L;
        Member member = memberRepository.findById(memberId).orElseThrow();
        Reply reply = replyRepository.findById(replyId).orElseThrow();

        // when
        Optional<ReplyLike> replyLike = likeReplyRepository.findByMemberAndReply(member, reply);

        // then
        assertTrue(replyLike.isEmpty(), "유저가 댓글에 좋아요를 누른적 없는 상황.");
    }

    @Test
    @DisplayName("댓글중에 유저가 좋아요 누른 댓글 id들을 리턴합니다. (성공)")
    void findLikedReplyIdsByMemberSuccessTest1(){
        // given
        Long memberId = 6L;
        Member member = memberRepository.findById(memberId).orElseThrow();
        List<Long> replyIds = LongStream.range(1,20).boxed().collect(Collectors.toList());

        // when
        List<Long> likedReplyIds = likeReplyRepository.findLikedReplyIdsByMember(member, replyIds);

        // then
        assertEquals(14,likedReplyIds.size(), "댓글중에 유저가 좋아요 누른 댓글은 14개입니다.");
    }

    @Test
    @DisplayName("댓글중에 유저가 좋아요 누른 댓글 id들을 리턴합니다. (댓글이 없는 경우)")
    void findLikedReplyIdsByMemberSuccessTest2(){
        // given
        Long memberId = 1L;
        Member member = memberRepository.findById(memberId).orElseThrow();
        List<Long> replyIds = LongStream.range(1,20).boxed().collect(Collectors.toList());

        // when
        List<Long> likedReplyIds = likeReplyRepository.findLikedReplyIdsByMember(member, replyIds);

        // then
        assertTrue(likedReplyIds.isEmpty(), "댓글중에 유저가 좋아요 누른 댓글은 없습니다.");
    }

}
