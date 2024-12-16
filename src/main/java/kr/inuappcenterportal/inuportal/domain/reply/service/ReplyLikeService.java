package kr.inuappcenterportal.inuportal.domain.reply.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.replylike.model.ReplyLike;
import kr.inuappcenterportal.inuportal.domain.replylike.repository.LikeReplyRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyLikeService {
    private final ReplyRepository replyRepository;
    private final LikeReplyRepository likeReplyRepository;
    public int likeReply(Member member, Long replyId){
        Reply reply = replyRepository.findById(replyId).orElseThrow(()->new MyException(MyErrorCode.REPLY_NOT_FOUND));
        if(reply.getMember()!=null&&reply.getMember().getId().equals(member.getId())){
            throw new MyException(MyErrorCode.NOT_LIKE_MY_REPLY);
        }
        if(likeReplyRepository.existsByMemberAndReply(member,reply)){
            ReplyLike replyLike = likeReplyRepository.findByMemberAndReply(member,reply).orElseThrow(()->new MyException(MyErrorCode.USER_OR_REPLY_NOT_FOUND));
            likeReplyRepository.delete(replyLike);
            return -1;
        }
        else {
            ReplyLike replyLike = ReplyLike.builder().member(member).reply(reply).build();
            likeReplyRepository.save(replyLike);
            return 1;
        }
    }
}
