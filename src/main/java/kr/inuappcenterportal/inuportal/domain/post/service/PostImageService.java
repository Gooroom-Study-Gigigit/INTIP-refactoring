package kr.inuappcenterportal.inuportal.domain.post.service;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostImageService {

    private final PostCommonService postCommonService;

    @Value("${imagePath}")
    private String path;

    @Transactional
    public Long saveImageLocal(Member member, Long postId, List<MultipartFile> images) throws IOException {
        Post post = postCommonService.findPostByIdOrThrow(postId);
        postCommonService.validateMemberAuthorization(post, member.getId());
        long imageCount = images.size();
        post.updateImageCount(imageCount);
        saveNewImages(postId, images);
        return postId;
    }

    public byte[] getImage(Long postId, Long imageId) throws IOException {
        String fileName = postId + "-" + imageId;
        Path filePath = Paths.get(path, fileName);
        return Files.readAllBytes(filePath);
    }

    @Transactional
    public void updateImageLocal(Long memberId, Long postId, List<MultipartFile> images) throws IOException {
        Post post = postCommonService.findPostByIdOrThrow(postId);
        postCommonService.validateMemberAuthorization(post, memberId);
        if (images != null) {
            deleteExistingImages(postId, post.getImageCount());
            saveNewImages(postId, images);
            post.updateImageCount(images.size());
        }
    }

    public void saveNewImages(Long postId, List<MultipartFile> images) throws IOException {
        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            String fileName = postId + "-" + (i + 1);
            Path filePath = Paths.get(path, fileName);
            Files.write(filePath, file.getBytes());
        }
    }

    public void deleteExistingImages(Long postId, long imageCount) throws IOException {
        for (int i = 1; i <= imageCount; i++) {
            String fileName = postId + "-" + i;
            Path filePath = Paths.get(path, fileName);
            Files.deleteIfExists(filePath);
        }
    }
}
