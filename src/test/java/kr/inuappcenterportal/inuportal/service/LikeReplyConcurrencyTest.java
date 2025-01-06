package kr.inuappcenterportal.inuportal.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.repository.MemberRepository;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyLikeService;
import kr.inuappcenterportal.inuportal.domain.replylike.repository.LikeReplyRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {
        "SET FOREIGN_KEY_CHECKS=0;",
        "TRUNCATE TABLE reply_like;",
        "TRUNCATE TABLE reply;",
        "TRUNCATE TABLE post;",
        "TRUNCATE TABLE member;",
        "SET FOREIGN_KEY_CHECKS=1;"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
public class LikeReplyConcurrencyTest {

    @Autowired
    private LikeReplyRepository likeReplyRepository;

    @Autowired
    private ReplyLikeService replyLikeService;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    public void before(){
        Member replyMember = Member.builder()
                .studentId("20202020")
                .nickname("강형준")
                .roles(Collections.singletonList("ROLE_USER"))
                .build();
        memberRepository.save(replyMember);

        Post post = Post.builder()
                .title("첫 번째 게시글")
                .content("게시글 내용")
                .anonymous(true)
                .category("카테고리")
                .member(replyMember)
                .build();
        postRepository.save(post);

        Reply reply = Reply.builder()
                .content("댓글 내용")
                .anonymous(true)
                .member(replyMember)
                .post(post)
                .build();
        replyRepository.save(reply);
    }

    @Test
    public void 좋아요동시성테스트() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Reply reply = replyRepository.findById(1L).orElseThrow();

        List<Member> members = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Member member = Member.builder()
                    .studentId("2020" + i)
                    .nickname("유저" + i)
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build();
            memberRepository.save(member);
            members.add(member);
        }

        for (Member member : members) {
            executorService.execute(() -> {
                try{
                    System.out.println(member.getId() + " 멤버와" + reply.getId() + "댓글 테스트");
                    replyLikeService.likeReply(member, reply.getId());
                }finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Reply updatedReply = replyRepository.findById(reply.getId())
                .orElseThrow(() -> new MyException(MyErrorCode.REPLY_NOT_FOUND));

        Assertions.assertEquals(100L, updatedReply.getLikeCount());
    }
}
