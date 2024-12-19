package kr.inuappcenterportal.inuportal.domain.member.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CookieUtilTest {

    private static final String COOKIE_NAME = "cookieName";
    private static final String COOKIE_VALUE = "cookieValue";
    private static final long COOKIE_EXPIRATION = 86400;

    @Test
    public void 쿠키를_정상적으로_발급한다() {
        // when
        ResponseCookie cookie = CookieUtil.createCookie(COOKIE_NAME, COOKIE_VALUE, COOKIE_EXPIRATION);

        // then
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo(COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(COOKIE_VALUE);
        assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(COOKIE_EXPIRATION);
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.isHttpOnly()).isTrue();
    }

    @Test
    public void 찾는_이름의_쿠키가_요청에서_조회된다() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] mockCookies = {new Cookie(COOKIE_NAME, COOKIE_VALUE)};

        // when
        when(request.getCookies()).thenReturn(mockCookies);
        Cookie cookie = CookieUtil.findCookieByName(request, COOKIE_NAME);

        // then
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo(COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(COOKIE_VALUE);
    }

    @Test
    public void 찾는_이름의_쿠키가_요청에_없을_경우_null을_반환한다() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] mockCookies = {new Cookie("differentCookie", COOKIE_VALUE)};
        when(request.getCookies()).thenReturn(mockCookies);

        // when
        Cookie cookie = CookieUtil.findCookieByName(request, COOKIE_NAME);

        // then
        assertThat(cookie).isNull();
    }
}