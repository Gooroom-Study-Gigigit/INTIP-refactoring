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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyQueryService {

    private final ReplyRepository replyRepository;
    private final PostRepository postRepository;
    private final LikeReplyRepository likeReplyRepository;

    // 해당 멤버가 작성한 모든 댓글을 조회합니다.
    public List<ReplyListResponseDto> getReplyByMember(Member reqMember,String sort){
        return replyRepository.findAllByMemberAndIsDeletedFalse(reqMember, sortReply(sort)).stream()
                .map(ReplyListResponseDto::of).collect(Collectors.toList());
    }

    public List<ReplyResponseDto> getReplies(Long postId, Member member) {
        Post post = postRepository.findById(postId).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        List<Reply> replies = replyRepository.findAllNonDeletedOrHavingChildren(post);
        Set<Long> likedReplyIds = getMemberLikedIds(replies,member);
        return replies.stream()
                .filter(reply -> reply.getReply() == null)
                .map(reply -> {
                    List<ReReplyResponseDto> reReplies = replies.stream()
                            .filter(reReply -> reReply.getReply() != null && reReply.getReply().getId().equals(reply.getId()))
                            .map(reReply -> {
                                boolean isLiked = likedReplyIds.contains(reReply.getId());
                                String writer = writerName(reReply,post);
                                long fireId = writer.equals("(알수없음)") ? 13 : reReply.getMember().getFireId();
                                return ReReplyResponseDto.of(reReply, writer, fireId, isLiked, hasAuthority(member, reReply));
                            }).collect(Collectors.toList());
                    boolean isLiked = likedReplyIds.contains(reply.getId());
                    String writer = writerName(reply,post);
                    long fireId = writer.equals("(알수없음)") ? 13 : reply.getMember().getFireId();
                    return ReplyResponseDto.of(reply, writer, fireId, isLiked, hasAuthority(member, reply), reReplies);
                })
                .collect(Collectors.toList());
    }

    public boolean hasAuthority(Member member, Reply reply){
        boolean hasAuthority = false;
        if(!reply.getIsDeleted()&&member!=null&&reply.getMember()!=null&&reply.getMember().getId().equals(member.getId())){
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


    public List<ReReplyResponseDto> getBestReplies(Long postId,Member member){
        Post post = postRepository.findById(postId).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        List<Reply> replies = replyRepository.findBestReplies(post);
        Set<Long> likedReplyIds = getMemberLikedIds(replies,member);
        return replies.stream().map(reply -> {
            String writer = writerName(reply,post);
            long fireId = writer.equals("(알수없음)")||writer.equals("(삭제됨)")?13: reply.getMember().getFireId();
            boolean isLiked = likedReplyIds.contains(reply.getId());
            boolean hasAuthority = hasAuthority(member,reply);
            return ReReplyResponseDto.of(reply,writer,fireId, isLiked,hasAuthority);
        }).collect(Collectors.toList());
    }

    private Set<Long> getMemberLikedIds(List<Reply> replies, Member member){
        Set<Long> likedReplyIds = new HashSet<>();
        if (member != null) {
            List<Long> replyIds = replies.stream()
                    .map(Reply::getId)
                    .collect(Collectors.toList());
            likedReplyIds.addAll(likeReplyRepository.findLikedReplyIdsByMember(member, replyIds));
        }
        return likedReplyIds;
    }

    private Sort sortReply(String sort){
        if(sort.equals("date")){
            return Sort.by(Sort.Direction.DESC, "id");
        }
        else if(sort.equals("like")){
            return Sort.by(Sort.Direction.DESC, "good","id");
        }
        else{
            throw new MyException(MyErrorCode.WRONG_SORT_TYPE);
        }
    }


}
