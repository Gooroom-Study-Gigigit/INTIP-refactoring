package kr.inuappcenterportal.inuportal.domain.reply.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.replylike.model.LikeAction;
import kr.inuappcenterportal.inuportal.domain.replylike.model.ReplyLike;
import kr.inuappcenterportal.inuportal.domain.replylike.repository.LikeReplyRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyLikeService {

    private final ReplyRepository replyRepository;
    private final LikeReplyRepository likeReplyRepository;

    // 댓글에 좋아요 또는 좋아요 취소를 합니다.
    public LikeAction likeReply(Member reqMember, Long targetReplyId){
        Reply targetReply = replyRepository.findById(targetReplyId).orElseThrow(()->new MyException(MyErrorCode.REPLY_NOT_FOUND));
        if(isMemberSameReplyAuthor(targetReply, reqMember)){
            throw new MyException(MyErrorCode.NOT_LIKE_MY_REPLY);
        }
        Optional<ReplyLike> replyLike = likeReplyRepository.findByMemberAndReply(reqMember, targetReply);
        if(replyLike.isEmpty()){    // 멤버가 해당 댓글에 좋아요가 되어있지 않은 경우
            ReplyLike newReplyLike = ReplyLike.builder().member(reqMember).reply(targetReply).build();
            likeReplyRepository.save(newReplyLike);
            targetReply.upLike();
            return LikeAction.LIKE;
        }else{    // 멤버가 해당 댓글에 좋아요가 되어있는 경우
            likeReplyRepository.delete(replyLike.get());
            targetReply.downLike();
            return LikeAction.UNLIKE;
        }
    }

    // 댓글 글쓴이와 좋아요를 누른 유저가 동일한지 판별하는 메서드
    private boolean isMemberSameReplyAuthor(Reply targetReply, Member reqMember){
        return targetReply.getMember()!=null && targetReply.getMember().getId().equals(reqMember.getId());
    }
}
