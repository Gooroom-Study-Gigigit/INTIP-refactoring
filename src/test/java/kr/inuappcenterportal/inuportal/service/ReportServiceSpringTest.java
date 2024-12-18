package kr.inuappcenterportal.inuportal.service;

import kr.inuappcenterportal.inuportal.domain.category.model.Category;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.member.repository.MemberRepository;
import kr.inuappcenterportal.inuportal.domain.post.model.Post;
import kr.inuappcenterportal.inuportal.domain.post.repository.PostRepository;
import kr.inuappcenterportal.inuportal.domain.report.dto.ReportListResponseDto;
import kr.inuappcenterportal.inuportal.domain.report.dto.ReportRequestDto;
import kr.inuappcenterportal.inuportal.domain.report.dto.ReportResponseDto;
import kr.inuappcenterportal.inuportal.domain.report.model.Report;
import kr.inuappcenterportal.inuportal.domain.report.repository.ReportRepository;
import kr.inuappcenterportal.inuportal.domain.report.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


@SpringBootTest
@Transactional
public class ReportServiceSpringTest {

    @Autowired
    ReportService reportService;

    @Autowired
    ReportRepository reportRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("부적절해 보이는 게시물을 신고합니다.")
    void saveReport() {
        //given
        Post post = createPost("게시물 제목", "게시물 내용", setCategory("인간 관계"));
        Member member = createMember("20241825", "진이");
        ReportRequestDto requestDto = new ReportRequestDto("인권 침해", "인권에 대한 모독, 비난 등 부적절한 내용입니다.");

        postRepository.save(post);
        memberRepository.save(member);

        //when
        Long result = reportService.saveReport(requestDto, post.getId(), member.getId());

        //then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Long.class);
    }


    @Test
    @DisplayName("게시물 신고 목록을 조회합니다.")
    void getReportList() {
        //given
        Post post1 = createPost("게시물 제목1", "게시물 내용1", setCategory("인간 관계"));
        Post post2 = createPost("게시물 제목2", "게시물 내용2", setCategory("사랑"));
        Post post3 = createPost("게시물 제목3", "게시물 내용3", setCategory("성"));
        Post post4 = createPost("게시물 제목4", "게시물 내용4", setCategory("꿀팁"));
        Member member1 = createMember("20241825", "진이");
        Member member2 = createMember("20231362", "순이");

        postRepository.saveAll(List.of(post1, post2, post3, post4));
        memberRepository.saveAll(List.of(member1, member2));

        Report report1 = createReport("이유는 너가 나빴어", "너가 사랑을 알고 떠드는거야? 부적절해.", post2.getId(), member1.getId());
        Report report2 = createReport("성희롱", "남에 대해 성희롱하여 비난함", post3.getId(), member1.getId());
        Report report3 = createReport("의미 없는 내용", "꿀팁인데 의미 없는 게시물입니다.", post4.getId(), member1.getId());
        Report report4 = createReport("성희롱", "성희롱을 통해 공개적으로 수치심을 줌.", post3.getId(), member2.getId());
        reportRepository.saveAll(List.of(report1, report2, report3, report4));

        int page = 1;

        //when
        ReportListResponseDto result = reportService.getReportList(page);

        //then
        assertThat(result)
                .extracting("pages", "total")
                .containsExactly(1, 4L);

        assertThat(result.getReports())
                .extracting("reason", "comment", "postId", "memberId")
                .containsExactlyInAnyOrder(
                        tuple("이유는 너가 나빴어", "너가 사랑을 알고 떠드는거야? 부적절해.", post2.getId(), member1.getId()),
                        tuple("성희롱", "남에 대해 성희롱하여 비난함", post3.getId(), member1.getId()),
                        tuple("의미 없는 내용", "꿀팁인데 의미 없는 게시물입니다.", post4.getId(), member1.getId()),
                        tuple("성희롱", "성희롱을 통해 공개적으로 수치심을 줌.", post3.getId(), member2.getId())
                );

    }

    private Report createReport(String reason, String comment, Long postId, Long memberId) {
        return Report.builder()
                .reason(reason)
                .comment(comment)
                .postId(postId)
                .memberId(memberId)
                .build();
    }

    private Category setCategory(String categoryName) {
        return Category.builder().category(categoryName).build();
    }

    private Member createMember(String studentId, String nickname) {
        return Member.builder()
                .studentId(studentId)
                .nickname(nickname)
                .build();
    }

    private Post createPost(String title, String content, Category category) {
        return Post.builder()
                .title(title)
                .content(content)
                .category(content)
                .build();
    }
}
