package kr.inuappcenterportal.inuportal.domain.reply.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReReplyResponseDto;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyListResponseDto;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyResponseDto;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.replylike.repository.LikeReplyRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyQueryService {

    private final ReplyRepository replyRepository;
    private final PostRepository postRepository;
    private final LikeReplyRepository likeReplyRepository;

    public List<ReplyListResponseDto> getReplyByMember(Member member, String sort){
        if(sort==null||sort.equals("date")) {
            return replyRepository.findAllByMemberOrderByIdDesc(member).stream().map(ReplyListResponseDto::of).collect(Collectors.toList());
        }
        else if(sort.equals("like")){
            return replyRepository.findAllByMemberOrderByIdDesc(member).stream().map(ReplyListResponseDto::of).sorted(Comparator.comparing(ReplyListResponseDto::getLike).reversed()).collect(Collectors.toList());
        }
        else{
            throw new MyException(MyErrorCode.WRONG_SORT_TYPE);
        }
    }
    public List<ReplyResponseDto> getReplies(Long postId, Member member){
        Post post = postRepository.findById(postId).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        List<Reply> replies = replyRepository.findAllByPostAndReplyIsNull(post);
        return replies.stream().map(reply -> {
                    List<ReReplyResponseDto> reReplyResponseDtoList = replyRepository.findAllByReply(reply).stream().map(reReply ->{
                                String writer = writerName(reReply,post);
                                boolean isLiked = isLiked(member,reReply);
                                boolean hasAuthority = hasAuthority(member,reReply);
                                long fireId = writer.equals("(알수없음)")||writer.equals("(삭제됨)")?13: reReply.getMember().getFireId();
                                return ReReplyResponseDto.of(reReply,writer,fireId,isLiked,hasAuthority);
                            })
                            .collect(Collectors.toList());
                    String writer = writerName(reply,post);
                    long fireId = writer.equals("(알수없음)")||writer.equals("(삭제됨)")?13: reply.getMember().getFireId();
                    boolean isLiked = isLiked(member,reply);
                    boolean hasAuthority = hasAuthority(member,reply);
                    return ReplyResponseDto.of(reply, writer, fireId, isLiked,hasAuthority,reReplyResponseDtoList);

                })
                .collect(Collectors.toList());
    }
    public List<ReReplyResponseDto> getBestReplies(Long postId,Member member){
        Post post = postRepository.findById(postId).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        List<Reply> likeList = replyRepository.findRepliesWithLikesByPost(post.getId());
        List<ReReplyResponseDto> bestReplies = likeList.stream()
                .limit(2) // 최대 2개 처리
                .map(reply -> {
                    String writer = writerName(reply, post);
                    long fireId = writer.equals("(알수없음)") || writer.equals("(삭제됨)") ? 13 : reply.getMember().getFireId();
                    boolean isLiked = isLiked(member, reply);
                    boolean hasAuthority = hasAuthority(member, reply);
                    return ReReplyResponseDto.of(reply, writer, fireId, isLiked, hasAuthority);
                })
                .toList();
        return bestReplies;
    }
    public boolean isLiked(Member member,Reply reply){
        boolean isLiked = false;
        if(member!=null&&likeReplyRepository.existsByMemberAndReply(member,reply)){
            isLiked = true;
        }
        return isLiked;
    }
    public boolean hasAuthority(Member member, Reply reply){
        boolean hasAuthority = false;
        if(member!=null&&reply.getMember()!=null&&reply.getMember().getId().equals(member.getId())){
            hasAuthority = true;
        }
        return hasAuthority;
    }
    public String writerName(Reply reply,Post post){
        String writer;
        if(reply.getIsDeleted()){
            writer="(삭제됨)";
        }
        else if(reply.getMember()==null){
            writer="(알수없음)";
        }
        else{
            if (reply.getAnonymous()) {
                if(reply.getMember().equals(post.getMember())){
                    writer = "횃불이(글쓴이)";
                }
                else {
                    writer = "횃불이"+reply.getNumber();
                }
            }
            else{
                writer = reply.getMember().getNickname();
            }
        }
        return writer;
    }
}
