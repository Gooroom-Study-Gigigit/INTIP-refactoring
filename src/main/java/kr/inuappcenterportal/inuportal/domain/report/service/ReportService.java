package kr.inuappcenterportal.inuportal.domain.report.service;

import kr.inuappcenterportal.inuportal.domain.report.dto.ReportListResponseDto;
import kr.inuappcenterportal.inuportal.domain.report.dto.ReportRequestDto;
import kr.inuappcenterportal.inuportal.domain.report.model.Report;
import kr.inuappcenterportal.inuportal.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;

    private final static int DEFAULT_PAGE_SIZE = 8;

    @Transactional
    public Long saveReport(ReportRequestDto reportRequestDto, Long postId, Long memberId) {
        Report report = Report.of(reportRequestDto.getReason(), reportRequestDto.getComment(), memberId, postId);
        return reportRepository.save(report).getId();
    }

    public ReportListResponseDto getReportList(int pageNumber) {
        Pageable pageable = convertPageRequestFrom(pageNumber, DEFAULT_PAGE_SIZE);
        return ReportListResponseDto.of(reportRepository.findAllBy(pageable));
    }

    private PageRequest convertPageRequestFrom(int pageNumber, int pageSize) {
        return PageRequest.of(pageNumber > 0 ? --pageNumber : pageNumber, pageSize);
    }
}
