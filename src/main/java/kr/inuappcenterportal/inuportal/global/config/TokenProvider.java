package kr.inuappcenterportal.inuportal.global.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyErrorCode;
import kr.inuappcenterportal.inuportal.global.exception.ex.MyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class TokenProvider {

    private final UserDetailsService userDetailsService;

    private final Key accessTokenSigningKey;
    private final Key refreshTokenSigningKey;
    private static final String AUTHORITIES_KEY = "roles";

    public static final String REDIS_PREFIX_REFRESH = "RT:";
    // TODO: 변경할 토큰 시간을 고민하고 설정 파일에서 받아오도록 해야한다
    public static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 1000L * 60 * 60 * 2 ;//2시간
    public static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 1000L * 60 * 60 * 24;
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    public TokenProvider(
            UserDetailsService userDetailsService,
            @Value("${jwtSecret}") String accessTokenSecret,
            @Value("${refreshSecret}") String refreshTokenSecret) {
        this.userDetailsService = userDetailsService;
        this.accessTokenSigningKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenSigningKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String id, List<String> roles, long accessTokenExpirationSeconds){

        Date now = new Date();
        Date accessExpiredTime = new Date(now.getTime() + accessTokenExpirationSeconds);

        return Jwts.builder()
                .setSubject(id)
                .claim(AUTHORITIES_KEY, roles)
                .setIssuedAt(now)
                .setExpiration(accessExpiredTime)
                .signWith(accessTokenSigningKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String id, long refreshTokenExpirationSeconds){

        Date now = new Date();
        Date refreshExpiredTime = new Date(now.getTime() + refreshTokenExpirationSeconds);

        return Jwts.builder()
                .setSubject(id)
                .setIssuedAt(now)
                .setExpiration(refreshExpiredTime)
                .signWith(refreshTokenSigningKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token){
        //log.info("토큰 인증 정보 조회 시작");
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUsername(token));
        //log.info("토큰 인증 정보 조회 완료 user:{}",userDetails.getUsername());
        //log.info("토큰 인증 정보 조회 완료 user:{}",userDetails.getAuthorities());
        return new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
    }

    public String getUsername(String token){
            //log.info("토큰으로 회원 정보 추출");
            String info = Jwts.parserBuilder().setSigningKey(accessTokenSigningKey).build().parseClaimsJws(token).getBody().getSubject();
            log.info("토큰으로 회원 정보 추출 완료 info:{}",info);
            return info;

    }

    public String getUsernameByRefresh(String token){
        return Jwts.parserBuilder().setSigningKey(refreshTokenSigningKey).build().parseClaimsJws(token).getBody().getSubject();
    }
    public String resolveToken(HttpServletRequest request){
        return request.getHeader("Auth");
    }

    public boolean validateToken(String token){
        //log.info("토큰 유효성 검증 시작");
        return valid(accessTokenSigningKey, token);
    }

    public boolean validateRefreshToken(String token){
        //log.info("리프래쉬 토큰 유효성 검증 시작");
        return valid(refreshTokenSigningKey, token);
    }
    private boolean valid(Key key, String token){
        try{
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        }catch (SignatureException ex){
            throw new MyException(MyErrorCode.WRONG_TYPE_TOKEN);
        }catch (MalformedJwtException ex){
            throw new MyException(MyErrorCode.UNSUPPORTED_TOKEN);
        }catch (ExpiredJwtException ex){
            throw new MyException(MyErrorCode.EXPIRED_TOKEN);
        }catch (IllegalArgumentException ex){
            throw new MyException(MyErrorCode.UNKNOWN_TOKEN_ERROR);
        }
    }
}
