package module;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.inuappcenterportal.inuportal.domain.member.controller.MemberController;
import kr.inuappcenterportal.inuportal.domain.member.service.MemberService;
import kr.inuappcenterportal.inuportal.domain.post.service.PostService;
import kr.inuappcenterportal.inuportal.domain.reply.controller.ReplyController;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyCommandService;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyLikeService;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyQueryService;
import kr.inuappcenterportal.inuportal.domain.report.service.ReportService;
import kr.inuappcenterportal.inuportal.global.config.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {MemberController.class, ReplyController.class} /* 추가로 필요한 컨트롤러 클래스 지정 */)
public abstract class ControllerTestSupport {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected MemberService memberService;

    @MockBean
    protected TokenProvider tokenProvider;

    @MockBean
    protected PostService postService;

    @MockBean
    protected ReplyQueryService replyQueryService;

    @MockBean
    protected ReplyCommandService replyCommandService;

    @MockBean
    protected ReplyLikeService replyLikeService;

    @MockBean
    protected ReportService reportService;
}

/**
 * 위는 @WebMvcTest 를 통합한 테스트 모듈입니다. 해당 모듈은 컨트롤러 단(Presentation Layer)
 * 만 검증하기 위한 용도로 Presentation Layer <-> Mocking(Business Layer <-> Persistence Layer)
 * 과 같은 구조입니다. IntegrationSupportTest 를 통해서 서비스 레이어와 리포지토리 레이어가 검증되었기에
 * 복잡한 테스트 과정을 제외하기 위해 컨트롤러 단만 따로 테스트하려 했습니다.
 * <p>
 * 만약 실제 API 호출에 대한 요청과 응답을 기준으로 동일한 시나리오로 테스트하려면 다른 방법을
 * 사용해야 됩니다. ( ex : 컨트롤러 -> 서비스 -> 리포지토리 전체 테스트)
 * <p>
 * 추가하고 싶은 컨트롤러가 있다면 맨 위 @WebMvcTest controllers의 항목에 추가하면 됩니다.)
 */