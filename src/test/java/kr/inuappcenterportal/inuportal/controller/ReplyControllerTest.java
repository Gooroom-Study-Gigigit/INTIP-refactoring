package kr.inuappcenterportal.inuportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.reply.controller.ReplyController;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyDto;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyCommandService;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyLikeService;
import kr.inuappcenterportal.inuportal.global.config.SecurityConfig;
import kr.inuappcenterportal.inuportal.global.config.TokenProvider;
import kr.inuappcenterportal.inuportal.custom.WithMockCustom;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ReplyController.class)
@MockBean(JpaMetamodelMappingContext.class)
@Import({SecurityConfig.class})
public class ReplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReplyCommandService replyCommandService;

    @MockBean
    private ReplyLikeService replyLikeService;

    @MockBean
    private TokenProvider tokenProvider;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockCustom //Custom MockUser
    @DisplayName("댓글 작성 테스트 -> 댓글 작성 (성공)")
    public void saveReplySuccessTest() throws Exception {
        // given
        Long targetPostId = 1L;
        ReplyDto replyDto = new ReplyDto("댓글 내용", true);
        String body = objectMapper.writeValueAsString(replyDto);

        when(replyCommandService.saveReply(any(Member.class), any(ReplyDto.class), any(Long.class)))
                .thenReturn(1L);

        // when & then
        mockMvc.perform(post("/api/replies/" + targetPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.msg").value("댓글 등록 성공"),
                        jsonPath("$.data").value(1L)
                )
                .andDo(print()); // 요청/응답 출력

        verify(replyCommandService, times(1))
                .saveReply(any(Member.class), any(ReplyDto.class), any(Long.class));
    }

    @Test
    @WithMockCustom //Custom MockUser
    @DisplayName("댓글 작성 테스트 -> 댓글 내용 공백 (실패)")
    public void saveReplyFailTest1() throws Exception {
        // given
        Long targetPostId = 1L;
        ReplyDto replyDto = new ReplyDto("", false);
        String body = objectMapper.writeValueAsString(replyDto);

        // when & then
        mockMvc.perform(post("/api/replies/" + targetPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.msg").value("must not be blank"),
                        jsonPath("$.data").value(-1L)
                )
                .andDo(print()); // 요청/응답 출력

        verify(replyCommandService, times(0))
                .saveReply(any(Member.class), any(ReplyDto.class), any(Long.class));
    }

    @Test
    @WithMockCustom //Custom MockUser
    @DisplayName("댓글 작성 테스트 -> anonymous 에 null 값 (실패)")
    public void saveReplyFailTest2() throws Exception {
        // given
        Long targetPostId = 1L;
        ReplyDto replyDto = new ReplyDto("댓글 작성",null);
        String body = objectMapper.writeValueAsString(replyDto);

        // when & then
        mockMvc.perform(post("/api/replies/" + targetPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.msg").value("must not be null"),
                        jsonPath("$.data").value(-1L)
                )
                .andDo(print()); // 요청/응답 출력

        verify(replyCommandService, times(0))
                .saveReply(any(Member.class), any(ReplyDto.class), any(Long.class));
    }

    @Test
    @WithMockCustom //Custom MockUser
    @DisplayName("댓글 작성 테스트 -> 존재하지 않는 게시글 id (실패)")
    public void saveReplyFailTest3() throws Exception {
        // given
        Long targetPostId = 1L;
        ReplyDto replyDto = new ReplyDto("댓글 작성",true);
        String body = objectMapper.writeValueAsString(replyDto);

        doThrow(new MyException(MyErrorCode.POST_NOT_FOUND))
                .when(replyCommandService).saveReply(any(Member.class), any(ReplyDto.class), any(Long.class));

        // when & then
        mockMvc.perform(post("/api/replies/" + targetPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.msg").value("존재하지 않는 게시글입니다."),
                        jsonPath("$.data").value(-1L)
                )
                .andDo(print()); // 요청/응답 출력

        verify(replyCommandService, times(1))
                .saveReply(any(Member.class), any(ReplyDto.class), any(Long.class));
    }

    @Test
    @WithMockCustom
    @DisplayName("대댓글 작성 테스트 -> 대댓글 작성(성공)")
    void saveReReplySuccessTest() throws Exception {
        // given
        Long targetReplyId = 1L;
        ReplyDto replyDto = new ReplyDto("대댓글 내용", true); // 대댓글 내용 및 익명 여부
        String body = objectMapper.writeValueAsString(replyDto); // RequestBody로 사용할 JSON 데이터

        when(replyCommandService.saveReReply(any(Member.class), any(ReplyDto.class), any(Long.class)))
                .thenReturn(1L);

        // when & then
        mockMvc.perform(post("/api/replies/" + targetReplyId + "/re-replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.msg").value("대댓글 저장 성공"),
                        jsonPath("$.data").value(1L)
                )
                .andDo(print());

        verify(replyCommandService, times(1))
                .saveReReply(any(Member.class), any(ReplyDto.class), any(Long.class));
    }
}
