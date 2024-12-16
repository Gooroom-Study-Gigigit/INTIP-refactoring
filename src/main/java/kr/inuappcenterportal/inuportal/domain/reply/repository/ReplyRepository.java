package kr.inuappcenterportal.inuportal.domain.reply.repository;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply,Long> {
    List<Reply> findAllByPostAndReplyIsNull(Post post);
    List<Reply> findAllByReply(Reply reply);
    List<Reply> findAllByPost(Post post);
    List<Reply> findAllByMember(Member member);
    List<Reply> findAllByMemberOrderByIdDesc(Member member);
    boolean existsByReply(Reply reply);
    boolean existsByMember(Member member);
    Optional<Reply> findByMember(Member member);

    Optional<Reply> findFirstByMember(Member member);

    // 하나의 게시물의 댓글 들 중에 좋아요가 많이 박힌 댓글 순으로 정렬
    @Query("SELECT r FROM Reply r " +
            "JOIN ReplyLike rl ON rl.reply = r " +
            "WHERE r.post.id = :postId " + // 특정 Post에 속한 댓글 필터링
            "GROUP BY r " +
            "HAVING COUNT(rl) > 0 " +      // 좋아요가 1개 이상인 댓글만
            "ORDER BY COUNT(rl) DESC")    // 좋아요 수 기준 내림차순 정렬
    List<Reply> findRepliesWithLikesByPost(@Param("postId") Long postId);

}
