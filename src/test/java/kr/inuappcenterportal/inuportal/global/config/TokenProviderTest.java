package kr.inuappcenterportal.inuportal.global.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenProviderTest {
    private TokenProvider tokenProvider;

    private static final String TEST_SECRET = "accessabcdefghijklmnopqrstuvwxyz";
    private static final String TEST_REFRESH_SECRET = "refreshabcdefghijklmnopqrstuvwxyz";
    private static final long TEST_EXPIRATION = 3600;
    private static final String TEST_SUBJECT = "1";
    private static final String TEST_ROLE = "ROLE_USER";

    @Mock
    private UserDetailsService userDetailsService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetailsService = mock(UserDetailsService.class);
        tokenProvider = new TokenProvider(
                userDetailsService,
                TEST_SECRET,
                TEST_REFRESH_SECRET,
                TEST_EXPIRATION,
                TEST_EXPIRATION
        );
        userDetails = new User(
                TEST_SUBJECT,
                "",
                Collections.singletonList(new SimpleGrantedAuthority(TEST_ROLE)));
    }
    
    @Test
    void accessToken을_생성한다() {
        //when
        String accessToken = tokenProvider.createAccessToken(TEST_SUBJECT, Collections.singletonList(TEST_ROLE), new Date());
        //then
        assertAll(
                () -> assertThat(accessToken).isNotNull(),
                () -> assertThat(parseTokenSubject(accessToken, TEST_SECRET)).isEqualTo(TEST_SUBJECT)
        );
    }
    
    @Test
    void refreshToken을_생성한다() {
        //when
        String refreshToken = tokenProvider.createRefreshToken(TEST_SUBJECT, new Date());
        //then
        assertAll(
                () -> assertThat(refreshToken).isNotNull(),
                () -> assertThat(parseTokenSubject(refreshToken, TEST_REFRESH_SECRET)).isEqualTo(TEST_SUBJECT)
        );
    }
    
    @Test
    void accessToken으로_권한정보를_조회한다() {
        //given
        String accessToken = tokenProvider.createAccessToken(TEST_SUBJECT, Collections.singletonList(TEST_ROLE), new Date());
        when(userDetailsService.loadUserByUsername(TEST_SUBJECT)).thenReturn(userDetails);
        //when
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        //then
        assertAll(
                () -> assertThat(authentication).isNotNull(),
                () -> assertThat(authentication.getName()).isEqualTo(TEST_SUBJECT),
                () -> assertThat(authentication.getAuthorities())
                        .hasSize(1)
                        .extracting(authority -> authority.getAuthority())
                        .containsExactly(TEST_ROLE)
        );
    }
    
    @Test
    void accessToken으로_subject를_조회한다() {
        //given
        String accessToken = tokenProvider.createAccessToken(TEST_SUBJECT, Collections.singletonList(TEST_ROLE), new Date());
        //when
        String subject = tokenProvider.getUsername(accessToken);
        //then
        assertThat(subject).isEqualTo(TEST_SUBJECT);
        
    }
    
    @Test
    void refreshToken으로_subject를_조회한다() {
        //given
        String refreshToken = tokenProvider.createRefreshToken(TEST_SUBJECT, new Date());
        //when
        String subject = tokenProvider.getUsernameByRefresh(refreshToken);
        //then
        assertThat(subject).isEqualTo(TEST_SUBJECT);
    }
    
    @Test
    void 요청의_accessToken을_추출한다() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(TokenProvider.AUTHORIZATION_HEADER))
                .thenReturn(TokenProvider.BEARER_PREFIX + "testAccessToken");
        //when
        String accessToken = tokenProvider.resolveToken(request);
        //then
        assertThat(accessToken).isEqualTo("testAccessToken");
    }

    @Test
    void 요청의_accessToken에_Bearer이_포함되지않으면_null을_반환한다() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(TokenProvider.AUTHORIZATION_HEADER)).thenReturn("noBearer testAccessToken");
        // when
        String result = tokenProvider.resolveToken(request);
        // then
        assertThat(result).isNull();
    }
    
    @Test
    void accessToken의_유효성을_검증한다() {
        //given
        String accessToken = tokenProvider.createAccessToken(TEST_SUBJECT, Collections.singletonList(TEST_ROLE), new Date());
        //when
        boolean isValid = tokenProvider.validateToken(accessToken);
        //then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void 만료된_accessToekn을_검증한다() {
        // given
        String expiredAccessToken = tokenProvider.createAccessToken(
                TEST_SUBJECT,
                Collections.singletonList(TEST_ROLE),
                new Date(System.currentTimeMillis() - TEST_EXPIRATION * 1000));
        // when & then
        assertThatThrownBy(() -> tokenProvider.validateToken(expiredAccessToken))
                .isInstanceOf(MyException.class)
                .hasMessageContaining(MyErrorCode.EXPIRED_TOKEN.getMessage());
    }
    
    @Test
    void refreshToken의_유효성을_검증한다() {
        //given
        String refreshToken = tokenProvider.createRefreshToken(TEST_SUBJECT, new Date());
        //when
        boolean isValid = tokenProvider.validateRefreshToken(refreshToken);
        //then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void 만료된_refreshToken을_검증한다() {
        // given
        String expiredRefreshToken = tokenProvider.createRefreshToken(
                TEST_SUBJECT,
                new Date(System.currentTimeMillis() - TEST_EXPIRATION * 1000));
        // when & then
        assertThatThrownBy(() -> tokenProvider.validateRefreshToken(expiredRefreshToken))
                .isInstanceOf(MyException.class)
                .hasMessageContaining(MyErrorCode.EXPIRED_TOKEN.getMessage());
    }

    private String parseTokenSubject(String token, String secretKey) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}