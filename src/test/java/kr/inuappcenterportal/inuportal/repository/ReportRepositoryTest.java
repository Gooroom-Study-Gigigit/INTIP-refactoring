package kr.inuappcenterportal.inuportal.repository;

import kr.inuappcenterportal.inuportal.domain.report.model.Report;
import kr.inuappcenterportal.inuportal.domain.report.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@Transactional
public class ReportRepositoryTest {

    @Autowired
    ReportRepository reportRepository;

    @Test
    @DisplayName("Report 객체에 대해 페이징 조회합니다.")
    void findAllBy() {
        //given
        Report report1 = createReport("이유1", "내용1", 1L, 1L);
        Report report2 = createReport("이유2", "내용2", 2L, 1L);
        Report report3 = createReport("이유3", "내용3", 3L, 1L);
        Report report4 = createReport("이유4", "내용4", 4L, 2L);
        Report report5 = createReport("이유5", "내용5", 4L, 2L);
        Report report6 = createReport("이유6", "내용6", 5L, 3L);
        Report report7 = createReport("이유7", "내용7", 6L, 4L);
        reportRepository.saveAll(List.of(report1, report2, report3, report4, report5, report6, report7));

        int pageNumber = 1;
        int pageSize = 4;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<Report> result = reportRepository.findAllBy(pageRequest);

        //then
        assertThat(result.hasNext()).isFalse();
        assertThat(result.getTotalElements()).isEqualTo(7);
        assertThat(result.getContent())
                .hasSize(3)
                .extracting("reason", "comment")
                .containsExactly(
                        tuple("이유5", "내용5"),
                        tuple("이유6", "내용6"),
                        tuple("이유7", "내용7")
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
}
