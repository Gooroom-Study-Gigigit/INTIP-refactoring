package kr.inuappcenterportal.inuportal.domain.member.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

public class CookieUtil {
    public static final int COOKIE_EXPIRATION_DELETE = 0;

    public static ResponseCookie createCookie(String name, String value, long cookieExpiration) {
        return ResponseCookie.from(name, value)
                .maxAge(cookieExpiration)
                .path("/")
                .sameSite("Strict")
                .httpOnly(true)
                .build();
    }

    public static Cookie findCookieByName(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }
}
