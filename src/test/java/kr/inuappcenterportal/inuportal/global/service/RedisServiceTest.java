package kr.inuappcenterportal.inuportal.global.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisService redisService;

    private static final String KEY = "RT:1234";
    private static final String REFRESH_TOKEN = "testRefreshToken";
    private static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 1000L * 60 * 60 * 24;

    @Test
    void RefreshToken이_Redis에_저장된다() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        redisService.saveRefreshToken(KEY, REFRESH_TOKEN, REFRESH_TOKEN_EXPIRATION_SECONDS);

        // then
        verify(valueOperations, times(1))
                .set(KEY, REFRESH_TOKEN, REFRESH_TOKEN_EXPIRATION_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void Key에_해당하는_RefreshToken이_Redis에서_조회된다() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn(REFRESH_TOKEN);

        // when
        String resultRefreshToken = redisService.getRefreshToken(KEY);

        // then
        assertThat(resultRefreshToken).isEqualTo(REFRESH_TOKEN);
        verify(valueOperations, times(1)).get(KEY);
    }

    @Test
    void 존재하지_않는_Key로_RefreshToken을_Redis에서_조회시_null을_반환한다() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn(null);

        // when
        String resultRefreshToken = redisService.getRefreshToken(KEY);

        // then
        assertThat(resultRefreshToken).isNull();
        verify(valueOperations, times(1)).get(KEY);
    }

    @Test
    void Redis에서_RefreshToken을_삭제한다() {
        // given
        when(redisTemplate.delete(KEY)).thenReturn(true);

        // when
        Boolean result = redisService.deleteRefreshToken(KEY);

        // then
        assertThat(result).isTrue();
        verify(redisTemplate, times(1)).delete(KEY);
    }

    @Test
    void Redis에서_RefreshToken_삭제에_실패한다() {
        // given
        when(redisTemplate.delete(KEY)).thenReturn(false);

        // when
        Boolean result = redisService.deleteRefreshToken(KEY);

        // then
        assertThat(result).isFalse();
        verify(redisTemplate, times(1)).delete(KEY);
    }
}