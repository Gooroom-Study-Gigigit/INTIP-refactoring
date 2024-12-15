package kr.inuappcenterportal.inuportal.repository;

import kr.inuappcenterportal.inuportal.domain.member.Member;
import kr.inuappcenterportal.inuportal.domain.reply.Reply;
import kr.inuappcenterportal.inuportal.domain.replylike.ReplyLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeReplyRepository extends JpaRepository<ReplyLike,Long> {
    boolean existsByMemberAndReply(Member member, Reply reply);
    Optional<ReplyLike> findByMemberAndReply(Member member, Reply reply);
}
