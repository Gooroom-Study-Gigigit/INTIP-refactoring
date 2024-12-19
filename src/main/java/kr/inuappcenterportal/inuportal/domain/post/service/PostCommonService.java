package kr.inuappcenterportal.inuportal.domain.post.service;

import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCommonService {

    private final PostRepository postRepository;

    Post findPostByIdOrThrow(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new MyException(MyErrorCode.POST_NOT_FOUND));
    }

    void validateMemberAuthorization(Post post, Long memberId) {
        if (!post.getMember().getId().equals(memberId)) {
            throw new MyException(MyErrorCode.HAS_NOT_POST_AUTHORIZATION);
        }
    }
}
