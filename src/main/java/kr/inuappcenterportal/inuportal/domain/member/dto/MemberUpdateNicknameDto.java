package kr.inuappcenterportal.inuportal.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원 닉네임 변경 요청Dto")
@Getter
@NoArgsConstructor
public class MemberUpdateNicknameDto {

    @Schema(description = "닉네임",example = "닉네임")
    @NotNull(message = "닉네임은 null일 수 없습니다.")
    @NotBlank(message = "닉네임이 입력되지 않았습니다.")
    private String nickname;

    @Schema(description = "횃불이 번호")
    @NotNull(message = "fireId은 null 일 수 없습니다.")
    @Min(value = 1, message = "fireId는 최소 1입니다.")
    @Max(value = 12, message = "fireId는 최대 12입니다.")
    private Long fireId;

    @Builder
    public MemberUpdateNicknameDto(String nickname, Long fireId){
        this.nickname = nickname;
        this.fireId = fireId;
    }
}
