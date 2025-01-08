package kr.inuappcenterportal.inuportal.domain.reply.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReReplyResponseDto;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyListResponseDto;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyResponseDto;
import kr.inuappcenterportal.inuportal.domain.reply.model.Reply;
import kr.inuappcenterportal.inuportal.domain.reply.repository.ReplyRepository;
import kr.inuappcenterportal.inuportal.domain.replylike.repository.ReplyLikeRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyQueryService {

    private final ReplyRepository replyRepository;
    private final PostRepository postRepository;
    private final ReplyLikeRepository likeReplyRepository;

    // 해당 멤버가 작성한 모든 댓글을 조회합니다.
    public List<ReplyListResponseDto> getReplyByMember(Member member,String sort){
        return replyRepository.findAllByMemberAndIsDeletedFalse(member, sortReply(sort)).stream()
                .map(ReplyListResponseDto::of).collect(Collectors.toList());
    }

    // 정렬 기준을 정합니다.
    private Sort sortReply(String sort){
        if(sort.equals("date")){
            return Sort.by(Sort.Direction.DESC, "id");
        }
        if(sort.equals("like")){
            return Sort.by(Sort.Direction.DESC, "likeCount","id");
        }
        throw new MyException(MyErrorCode.WRONG_SORT_TYPE);
    }

    // 게시글에 해당하는 댓글, 대댓글을 조회합니다.
    public List<ReplyResponseDto> getRepliesByPost(Long postId, Member member) {
        Post post = postRepository.findById(postId).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        List<Reply> replies = replyRepository.findAllNonDeletedOrHavingChildren(post);
        Set<Long> likedReplyIds = getMemberLikedIds(replies,member);

        return replies.stream()
                .filter(reply -> reply.getReply() == null) // 부모댓글만 처리 대상에 포함.
                .map(reply -> {
                    List<ReReplyResponseDto> reReplies = findReReplies(reply, replies, likedReplyIds, post, member);
                    boolean isLiked = likedReplyIds.contains(reply.getId());
                    String writer = writerName(reply,post);
                    long fireId = writer.equals("(알수없음)") ? 13 : reply.getMember().getFireId(); // 댓글 프로필 이미지 id값
                    return ReplyResponseDto.of(reply, writer, fireId, isLiked, hasAuthority(member, reply), reReplies);
                }).toList();
    }

    // 부모 댓글에 작성된 대댓글을 추출합니다.
    private List<ReReplyResponseDto> findReReplies(Reply parentReply, List<Reply> replies, Set<Long> likedReplyIds, Post post, Member member) {
        return replies.stream()
                .filter(reReply -> reReply.getReply() != null && reReply.getReply().getId().equals(parentReply.getId()))
                .map(reReply -> {
                    boolean isLiked = likedReplyIds.contains(reReply.getId()); // 내가 좋아요를 누른 댓글인지 확인
                    String writer = writerName(reReply, post); // 대댓글 이름 처리
                    long fireId = writer.equals("(알수없음)") ? 13 : reReply.getMember().getFireId(); // 댓글 프로필 이미지 id값
                    return ReReplyResponseDto.of(reReply, writer, fireId, isLiked, hasAuthority(member, reReply));
                }).toList();
    }

    // 좋아요 개수가 많은 순으로 댓글을 조회합니다. ReReplyResponseDto 에는 reply, reReply 둘다 포함
    public List<ReReplyResponseDto> getBestReplies(Long postId, Member member){
        Post post = postRepository.findById(postId).orElseThrow(()->new MyException(MyErrorCode.POST_NOT_FOUND));
        List<Reply> replies = replyRepository.findBestReplies(post);
        Set<Long> likedReplyIds = getMemberLikedIds(replies, member);
        return replies.stream().map(reply -> {
            String writer = writerName(reply,post);
            long fireId = writer.equals("(알수없음)")||writer.equals("(삭제됨)")?13: reply.getMember().getFireId();
            boolean isLiked = likedReplyIds.contains(reply.getId());
            boolean hasAuthority = hasAuthority(member, reply);
            return ReReplyResponseDto.of(reply,writer,fireId, isLiked,hasAuthority);
        }).collect(Collectors.toList());
    }

    // 사용자가 자신의 댓글인지 여부를 판단하여, 수정, 또는 삭제 여부를 반환
    private boolean hasAuthority(Member member, Reply reply){
        boolean hasAuthority = false;
        if(!reply.getIsDeleted() && member!=null && reply.getMember() != null && reply.getMember().getId().equals(member.getId())){
            hasAuthority = true;
        }
        return hasAuthority;
    }

    // 댓글 작성자의 이름을 처리합니다. 삭제됨, 알수없음, 횃불이(글쓴이), 횃불이(번호), 본인 nickname
    private String writerName(Reply reply,Post post){
        if(reply.getIsDeleted()){
            return "(삭제됨)";
        }
        if(reply.getMember()==null){
            return "(알수없음)";
        }
        if(reply.getAnonymous()) {
            if(reply.getMember().equals(post.getMember())){
                return "횃불이(글쓴이)";
            }
            return "횃불이"+reply.getNumber();
        }
        return reply.getMember().getNickname();
    }

    // 자신이 좋아요를 누른 댓글들을 id 들을 파악합니다.
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
}
