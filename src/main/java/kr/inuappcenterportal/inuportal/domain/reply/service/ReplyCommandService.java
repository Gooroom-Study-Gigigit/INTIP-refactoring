package kr.inuappcenterportal.inuportal.domain.reply.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyDto;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import kr.inuappcenterportal.inuportal.global.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyCommandService {

    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    private final RedisService redisService;

    // 게시글에 댓글을 작성합니다.
    public Long saveReply(Member member, ReplyDto replyDto, Long postId) throws NoSuchAlgorithmException {
        Post post = postRepository.findById(postId).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        String hash = member.getId() + replyDto.getContent();
        redisService.blockRepeat(hash);
        long num = replyDto.getAnonymous() ? countAnonymousNumber(member,post) : 0; // 익명 댓글이 아니면 해당 로직을 실행시킬 이유가 없기에 조건을 건다.
        Reply reply = Reply.builder().content(replyDto.getContent()).anonymous(replyDto.getAnonymous()).member(member).post(post).number(num).build();
        replyRepository.save(reply);
        post.upReplyCount();
        return reply.getId();
    }

    // 댓글에 대댓글을 작성합니다. 대댓글에 대댓글 작성은 불가능합니다.
    public Long saveReReply(Member member, ReplyDto replyDto, Long replyId) throws NoSuchAlgorithmException {
        Reply reply = replyRepository.findById(replyId).orElseThrow(()->new MyException(MyErrorCode.REPLY_NOT_FOUND));
        String hash = member.getId() + replyDto.getContent();
        redisService.blockRepeat(hash);
        if(reply.getReply()!=null){
            throw new MyException(MyErrorCode.NOT_REPLY_ON_REREPLY);
        }
        Post post = postRepository.findById(reply.getPost().getId()).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        long num = replyDto.getAnonymous() ? countAnonymousNumber(member,post) : 0; // 익명 댓글이 아니면 해당 로직을 실행시킬 이유가 없기에 조건을 건다.
        Reply reReply = Reply.builder().content(replyDto.getContent()).anonymous(replyDto.getAnonymous()).member(member).reply(reply).post(post).number(num).build();
        post.upReplyCount();
        return replyRepository.save(reReply).getId();
    }

    // 익명 번호를 부여하기 위한 메서드 입니다.
    private long countAnonymousNumber(Member member, Post post){
        // 익명인데 글쓴이와 같다면 번호를 매길 필요 없음
        if (isAnonymousSamePostAuthor(post,member)) {
            return 0;
        }

        // 해당 멤버가 해당 게시물에 작성한 댓글 조회 후 번호 계산
        return replyRepository.findFirstByPostAndMember(post, member)
                .map(Reply::getNumber) // 게시글에 댓글을 단 적이 있으면 번호 반환
                .orElseGet(() -> { // 게시글에 댓글을 단 적이 없으면 게시물 익명 번호 증가 후 해당 번호 반환
                    post.upNumber();
                    return post.getNumber();
                });
    }

    // 익명 유저와 글쓴이가 같은지 판단하는 메서드
    private boolean isAnonymousSamePostAuthor(Post post, Member member) {
        return post.getMember() != null && member.getId().equals(post.getMember().getId());
    }

    // 댓글을 수정하는 메서드
    public Long updateReply(Long memberId, ReplyDto replyDto, Long replyId){
        Reply reply = replyRepository.findById(replyId).orElseThrow(()->new MyException(MyErrorCode.REPLY_NOT_FOUND));
        // 댓글의 주인이 맞는지 확인하는 조건문
        if(!reply.getMember().getId().equals(memberId)){
            throw new MyException(MyErrorCode.HAS_NOT_REPLY_AUTHORIZATION);
        }
        else{
            reply.update(replyDto.getContent(), replyDto.getAnonymous());
            return reply.getId();
        }
    }

    // 댓글을 삭제하는 메서드
    public void delete(Long memberId, Long replyId){
        Reply reply = replyRepository.findById(replyId).orElseThrow(()->new MyException(MyErrorCode.REPLY_NOT_FOUND));
        Post post = postRepository.findById(reply.getPost().getId()).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        // 댓글의 주인이 맞는지 확인하는 조건문
        if(!reply.getMember().getId().equals(memberId)){
            throw new MyException(MyErrorCode.HAS_NOT_REPLY_AUTHORIZATION);
        }
        else{
            post.downReplyCount();
            reply.onDelete();
        }
    }
}
