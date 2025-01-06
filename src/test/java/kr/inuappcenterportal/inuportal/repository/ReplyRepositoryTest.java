package kr.inuappcenterportal.inuportal.repository;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.repository.MemberRepository;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyLikeService;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Autowired
    private ReplyLikeService replyLikeService;

    @Test
    void Sql_파일실행테스트(){
        Member member = memberRepository.findById(1L).orElseThrow();
        System.out.println("member 1의 id : " + member.getId());
        Assertions.assertNotNull(member);
    }

    @Test
    @DisplayName("유저의 모든 댓글을 생성시간 정렬 기준으로 조회합니다.")
    void findAllByMemberAndIsDeletedFalseTest1(){
        // given
        Member member = memberRepository.findById(1L).orElseThrow();
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
    @DisplayName("유저의 모든 댓글을 좋아요 개수 내림차순 정렬 기준으로 조회합니다.")
    void findAllByMemberAndIsDeletedFalseTest2(){
        // given
        Member member = memberRepository.findById(1L).orElseThrow();
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
    @DisplayName("유저가 게시글에 작성한 첫 번째 댓글 가져오기")
    void findFirstByPostAndMember(){
        // given
        Member member = memberRepository.findById(1L).orElseThrow();
        Post post = postRepository.findById(1L).orElseThrow();
        System.out.println("member 1의 id : " + member.getId());

        //when
        Optional<Reply> reply = replyRepository.findFirstByPostAndMember(post, member);
        System.out.println("reply id : " + reply.get().getId());

        //then
        assertFalse(reply.isEmpty(), "댓글이 empty 가 아닙니다.");
        assertTrue(reply.get().getNumber() > 0,"number 가 0보다 큽니다.");
    }



}
