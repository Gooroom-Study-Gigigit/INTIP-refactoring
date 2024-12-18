package kr.inuappcenterportal.inuportal.domain.reply.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyDto;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.global.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyCommandService {
    private final ReplyRepository replyRepository;
    private final PostRepository postRepository;
    private final RedisService redisService;

    public Long saveReply(Member member, ReplyDto replyDto, Long postId) throws NoSuchAlgorithmException {
        Post post = postRepository.findById(postId).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        String hash = member.getId() + replyDto.getContent();
        redisService.blockRepeat(hash);
        long num = countNumber(member,post);
        Reply reply = Reply.builder().content(replyDto.getContent()).anonymous(replyDto.getAnonymous()).member(member).post(post).number(num).build();
        replyRepository.save(reply);
        post.upReplyCount();
        return reply.getId();
    }

    public Long saveReReply(Member member, ReplyDto replyDto, Long replyId) throws NoSuchAlgorithmException {
        Reply reply = replyRepository.findById(replyId).orElseThrow(()->new MyException(MyErrorCode.REPLY_NOT_FOUND));
        String hash = member.getId() + replyDto.getContent();
        redisService.blockRepeat(hash);
        if(reply.getReply()!=null){
            throw new MyException(MyErrorCode.NOT_REPLY_ON_REREPLY);
        }
        Post post = postRepository.findById(reply.getPost().getId()).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        long num = countNumber(member,post);
        Reply reReply = Reply.builder().content(replyDto.getContent()).anonymous(replyDto.getAnonymous()).member(member).reply(reply).post(post).number(num).build();
        post.upReplyCount();
        return replyRepository.save(reReply).getId();
    }

    public long countNumber(Member member, Post post){
        long num = 0;
        if(post.getMember()!=null) {
            if (!member.getId().equals(post.getMember().getId()) && replyRepository.existsByMember(member)) {
                Reply preReply = replyRepository.findFirstByMember(member).orElseThrow(() -> new MyException(MyErrorCode.REPLY_NOT_FOUND));
                num = preReply.getNumber();
            } else if (!member.getId().equals(post.getMember().getId())) {
                post.upNumber();
                num = post.getNumber();
            }
        }
        else{
            if( replyRepository.existsByMember(member)){
                Reply preReply = replyRepository.findFirstByMember(member).orElseThrow(() -> new MyException(MyErrorCode.REPLY_NOT_FOUND));
                num = preReply.getNumber();
            }
            else{
                post.upNumber();
                num = post.getNumber();
            }
        }
        return num;
    }

    public Long updateReply(Long memberId, ReplyDto replyDto, Long replyId){
        Reply reply = replyRepository.findById(replyId).orElseThrow(()->new MyException(MyErrorCode.REPLY_NOT_FOUND));
        if(!reply.getMember().getId().equals(memberId)){
            throw new MyException(MyErrorCode.HAS_NOT_REPLY_AUTHORIZATION);
        }
        else{
            reply.update(replyDto.getContent(), replyDto.getAnonymous());
            return reply.getId();
        }
    }

    public void delete(Long memberId, Long replyId){
        Reply reply = replyRepository.findById(replyId).orElseThrow(()->new MyException(MyErrorCode.REPLY_NOT_FOUND));
        Post post = postRepository.findById(reply.getPost().getId()).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        if(!reply.getMember().getId().equals(memberId)){
            throw new MyException(MyErrorCode.HAS_NOT_REPLY_AUTHORIZATION);
        }
        else{
            post.downReplyCount();
            if(replyRepository.existsByReply(reply)) {
                reply.onDelete("삭제된 댓글입니다.", null);
            }
            else replyRepository.delete(reply);
        }
    }
}
