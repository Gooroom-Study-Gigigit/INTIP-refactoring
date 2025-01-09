package kr.inuappcenterportal.inuportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.inuappcenterportal.inuportal.util.WithMockCustom;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import kr.inuappcenterportal.inuportal.domain.reply.controller.ReplyController;
import kr.inuappcenterportal.inuportal.domain.reply.dto.ReplyDto;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyCommandService;
import kr.inuappcenterportal.inuportal.domain.reply.service.ReplyLikeService;
import kr.inuappcenterportal.inuportal.domain.replylike.model.LikeAction;
import kr.inuappcenterportal.inuportal.global.config.SecurityConfig;
import kr.inuappcenterportal.inuportal.global.config.TokenProvider;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import module.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockBean(JpaMetamodelMappingContext.class)
@Import({SecurityConfig.class})
public class ReplyControllerTest extends ControllerTestSupport {

//
//    @MockBean
//    private ReplyCommandService replyCommandService;
//
//    @MockBean
//    private ReplyLikeService replyLikeService;
//
//    @MockBean
//    private TokenProvider tokenProvider;
//
//    private ObjectMapper objectMapper = new ObjectMapper();

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

    @Test
    @WithMockCustom
    @DisplayName("대댓글 작성 테스트 -> 댓글 내용 공백 (실패)")
    void saveReReplyFail1Test() throws Exception {
        // given
        Long targetReplyId = 1L;
        ReplyDto replyDto = new ReplyDto("", true); // 대댓글 내용 및 익명 여부
        String body = objectMapper.writeValueAsString(replyDto); // RequestBody로 사용할 JSON 데이터

        // when & then
        mockMvc.perform(post("/api/replies/" + targetReplyId + "/re-replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.msg").value("must not be blank"),
                        jsonPath("$.data").value(-1L)
                )
                .andDo(print());

        verifyNoInteractions(replyCommandService);
    }

    @Test
    @WithMockCustom
    @DisplayName("대댓글 작성 테스트 -> anonymous 에 null 값 (실패)")
    void saveReReplyFail2Test() throws Exception {
        // given
        Long targetReplyId = 1L;
        ReplyDto replyDto = new ReplyDto("대댓글 내용", null); // 대댓글 내용 및 익명 여부
        String body = objectMapper.writeValueAsString(replyDto); // RequestBody로 사용할 JSON 데이터

        // when & then
        mockMvc.perform(post("/api/replies/" + targetReplyId + "/re-replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.msg").value("must not be null"),
                        jsonPath("$.data").value(-1L)
                )
                .andDo(print());

        verifyNoInteractions(replyCommandService);
    }

    @Test
    @WithMockCustom
    @DisplayName("대댓글 작성 테스트 -> 존재하지 않는 게시글 id (실패)")
    void saveReReplyFail3Test() throws Exception {
        // given
        Long targetReplyId = 1L;
        ReplyDto replyDto = new ReplyDto("대댓글 내용", true); // 대댓글 내용 및 익명 여부
        String body = objectMapper.writeValueAsString(replyDto); // RequestBody로 사용할 JSON 데이터

        doThrow(new MyException(MyErrorCode.POST_NOT_FOUND))
                .when(replyCommandService).saveReReply(any(Member.class), any(ReplyDto.class), any(Long.class));

        // when & then
        mockMvc.perform(post("/api/replies/" + targetReplyId + "/re-replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.msg").value("존재하지 않는 게시글입니다."),
                        jsonPath("$.data").value(-1L)
                )
                .andDo(print());

        verify(replyCommandService, times(1))
                .saveReReply(any(Member.class), any(ReplyDto.class), any(Long.class));
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 수정 테스트 - 댓글 수정(성공)")
    void updateReplySuccessTest() throws Exception {
        // given
        Long replyId = 1L;
        ReplyDto replyDto = new ReplyDto("수정할 댓글 내용", true);
        String body = objectMapper.writeValueAsString(replyDto); // RequestBody로 사용할 JSON 데이터

        when(replyCommandService.updateReply(any(Long.class), any(ReplyDto.class), any(Long.class)))
                .thenReturn(1L);

        // when & then
        mockMvc.perform(put("/api/replies/"+ replyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1L))
                .andExpect(jsonPath("$.msg").value("댓글 수정 성공"))
                .andDo(print());

        verify(replyCommandService, times(1))
                .updateReply(any(Long.class), any(ReplyDto.class), any(Long.class));
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 수정 테스트 - 댓글 내용 공백(실패)")
    void updateReplyFailTest1() throws Exception{
        // given
        Long replyId = 1L;
        ReplyDto replyDto = new ReplyDto("", true);
        String body = objectMapper.writeValueAsString(replyDto); // RequestBody로 사용할 JSON 데이터


        // when & then
        mockMvc.perform(put("/api/replies/"+ replyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").value(-1L))
                .andExpect(jsonPath("$.msg").value("must not be blank"))
                .andDo(print());

        verifyNoInteractions(replyCommandService);
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 수정 테스트 - anonymous 에 null 값(실패)")
    void updateReplyFailTest2() throws Exception{
        // given
        Long replyId = 1L;
        ReplyDto replyDto = new ReplyDto("수정할 댓글 내용", null);
        String body = objectMapper.writeValueAsString(replyDto); // RequestBody로 사용할 JSON 데이터


        // when & then
        mockMvc.perform(put("/api/replies/"+ replyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").value(-1L))
                .andExpect(jsonPath("$.msg").value("must not be null"))
                .andDo(print());

        verifyNoInteractions(replyCommandService);
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 수정 테스트 - 존재하지 않는 댓글 수정(실패)")
    void updateReplyFailTest3() throws Exception{
        // given
        Long replyId = 100L;
        ReplyDto replyDto = new ReplyDto("수정할 댓글 내용", true);
        String body = objectMapper.writeValueAsString(replyDto); // RequestBody로 사용할 JSON 데이터

        doThrow(new MyException(MyErrorCode.REPLY_NOT_FOUND))
                .when(replyCommandService).updateReply(any(Long.class), any(ReplyDto.class), any(Long.class));


        // when & then
        mockMvc.perform(put("/api/replies/"+ replyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value(-1L))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 댓글입니다."))
                .andDo(print());

        verify(replyCommandService, times(1))
                .updateReply(any(Long.class), any(ReplyDto.class), any(Long.class));
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 수정 테스트 - 권한이 없는 멤버의 댓글 수정(실패)")
    void updateReplyFailTest4() throws Exception{
        // given
        Long replyId = 1L;
        ReplyDto replyDto = new ReplyDto("수정할 댓글 내용", true);
        String body = objectMapper.writeValueAsString(replyDto); // RequestBody로 사용할 JSON 데이터

        doThrow(new MyException(MyErrorCode.HAS_NOT_REPLY_AUTHORIZATION))
                .when(replyCommandService).updateReply(any(Long.class), any(ReplyDto.class), any(Long.class));


        // when & then
        mockMvc.perform(put("/api/replies/"+ replyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data").value(-1L))
                .andExpect(jsonPath("$.msg").value("이 댓글의 수정/삭제에 대한 권한이 없습니다."))
                .andDo(print());

        verify(replyCommandService, times(1))
                .updateReply(any(Long.class), any(ReplyDto.class), any(Long.class));
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 좋아요 테스트 - 댓글 좋아요 생성(성공)")
    void likeReplySuccessTest1() throws Exception{
        // given
        Long replyId = 1L;
        when(replyLikeService.likeReply(any(Member.class), any(Long.class)))
                .thenReturn(LikeAction.LIKE);

        // when & then
        mockMvc.perform(put("/api/replies/"+ replyId + "/like")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("LIKE"))
                .andExpect(jsonPath("$.msg").value("댓글 좋아요 여부 변경성공"))
                .andDo(print());

        verify(replyLikeService, times(1))
                .likeReply(any(Member.class), any(Long.class));
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 좋아요 테스트 - 댓글 좋아요 취소(성공)")
    void likeReplySuccessTest2() throws Exception{
        // given
        Long replyId = 1L;
        when(replyLikeService.likeReply(any(Member.class), any(Long.class)))
                .thenReturn(LikeAction.UNLIKE);

        // when & then
        mockMvc.perform(put("/api/replies/"+ replyId + "/like")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("UNLIKE"))
                .andExpect(jsonPath("$.msg").value("댓글 좋아요 여부 변경성공"))
                .andDo(print());

        verify(replyLikeService, times(1))
                .likeReply(any(Member.class), any(Long.class));
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 좋아요 테스트 - 존재하지 않는 댓글 좋아요(실패)")
    void likeReplyFailTest1() throws Exception{
        // given
        Long replyId = 100L;
        doThrow(new MyException(MyErrorCode.REPLY_NOT_FOUND))
                .when(replyLikeService).likeReply(any(Member.class), any(Long.class));

        // when & then
        mockMvc.perform(put("/api/replies/"+ replyId + "/like")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value(-1L))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 댓글입니다."))
                .andDo(print());

        verify(replyLikeService, times(1))
                .likeReply(any(Member.class), any(Long.class));
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 좋아요 테스트 - 자기 자신 댓글 좋아요(실패)")
    void likeReplyFailTest2() throws Exception{
        // given
        Long replyId = 1L;
        doThrow(new MyException(MyErrorCode.NOT_LIKE_MY_REPLY))
                .when(replyLikeService).likeReply(any(Member.class), any(Long.class));

        // when & then
        mockMvc.perform(put("/api/replies/"+ replyId + "/like")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").value(-1L))
                .andExpect(jsonPath("$.msg").value("자신의 댓글에는 추천을 할 수 없습니다."))
                .andDo(print());

        verify(replyLikeService, times(1))
                .likeReply(any(Member.class), any(Long.class));
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 삭제 테스트 - 댓글 삭제하기 (성공)")
    void deleteReplySuccessTest() throws Exception{
        // given
        Long replyId = 1L;

        // when & then
        mockMvc.perform(delete("/api/replies/"+ replyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1L))
                .andExpect(jsonPath("$.msg").value("댓글 삭제 성공"))
                .andDo(print());

        verify(replyCommandService, times(1))
                .delete(any(Long.class), any(Long.class));
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 삭제 테스트 - 권한이 없는 멤버의 댓글 삭제(실패)")
    void deleteReplyFail1Test() throws Exception{
        // given
        Long replyId = 1L;
        doThrow(new MyException(MyErrorCode.HAS_NOT_REPLY_AUTHORIZATION))
                .when(replyCommandService).delete(any(Long.class), any(Long.class));

        // when & then
        mockMvc.perform(delete("/api/replies/"+ replyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data").value(-1L))
                .andExpect(jsonPath("$.msg").value("이 댓글의 수정/삭제에 대한 권한이 없습니다."))
                .andDo(print());

        verify(replyCommandService, times(1))
                .delete(any(Long.class), any(Long.class));
    }

    @Test
    @WithMockCustom
    @DisplayName("댓글 삭제 테스트 - 없는 댓글 삭제(실패)")
    void deleteReplyFail2Test() throws Exception{
        // given
        Long replyId = 1L;
        doThrow(new MyException(MyErrorCode.REPLY_NOT_FOUND))
                .when(replyCommandService).delete(any(Long.class), any(Long.class));

        // when & then
        mockMvc.perform(delete("/api/replies/"+ replyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value(-1L))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 댓글입니다."))
                .andDo(print());

        verify(replyCommandService, times(1))
                .delete(any(Long.class), any(Long.class));
    }
}
