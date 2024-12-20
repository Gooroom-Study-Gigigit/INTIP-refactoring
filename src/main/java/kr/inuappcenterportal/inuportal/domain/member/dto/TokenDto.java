package kr.inuappcenterportal.inuportal.domain.member.dto;

import lombok.Builder;
import lombok.Getter;


@Getter
public class TokenDto {
    private final String accessToken;
    private final String refreshToken;

    @Builder
    private TokenDto(String accessToken,String refreshToken){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static TokenDto of(String accessToken, String refreshToken){
        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
