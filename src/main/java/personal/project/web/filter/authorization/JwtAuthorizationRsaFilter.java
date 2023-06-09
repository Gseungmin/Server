package personal.project.web.filter.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import personal.project.exception.ErrorResult;
import personal.project.exception.ErrorType;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static personal.project.exception.ErrorType.TOKEN_INVALID;
import static personal.project.exception.ErrorType.TOKEN_NOT_EXIST;

/**
 * Bearer 토큰을 RSA 알고리즘에 의해 검증하며 검증 성공시 인증 및 인가를 처리하는 필터
 */
public class JwtAuthorizationRsaFilter extends OncePerRequestFilter {

    private RSAKey jwk;

    public JwtAuthorizationRsaFilter(RSAKey rsaKey) {
        this.jwk = rsaKey;
    }

    /**인가를 거치지 않도록 해주는 메소드*/
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        request.getMethod();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return (pathMatcher.match("/auth/join", path)
                || pathMatcher.match("/auth/kakao", path)
                || pathMatcher.match("/search", path)
                || pathMatcher.match("/comments/**", path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        ErrorType errorType = null;

        //헤더 검사
        if (tokenResolve(request, response, chain)){
            errorType = TOKEN_NOT_EXIST;
        } else {
            String token = getToken(request); //Bearer를 제거한 토큰 값만 추출(header + payload + signature)
            SignedJWT signedJWT;
            try {

                signedJWT = SignedJWT.parse(token); //header와 payload와 signature 값이 속성으로 매핑됨
                RSASSAVerifier jwsVerifier = new RSASSAVerifier(jwk.toRSAPublicKey());
                boolean verify = signedJWT.verify(jwsVerifier);

                if (verify) {
                    String username = signedJWT.getJWTClaimsSet().getClaim("id").toString();
                    List<String> authority = (List) signedJWT.getJWTClaimsSet().getClaim("role");
                    //사용자 정보를 만들어서 인증 객체 생성 후 Security Context에 보관
                    if (username != null) {
                        UserDetails user = User.builder().username(username)
                                .password(UUID.randomUUID().toString())
                                .authorities(authority.get(0))
                                .build();
                        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                    errorType = TOKEN_INVALID;
                }
            } catch (Exception e) {
                errorType = TOKEN_INVALID;
            }
        }
        //예외가 발생할 경우 예외 저장
        if (errorType != null) {
            request.setAttribute("exception", errorType);
        }
        chain.doFilter(request, response); //다음 필터로 넘어감
    }

    /**Authorization 헤더 명으로 값이 넘어옴*/
    protected String getToken(HttpServletRequest request) {
        return request.getHeader("Authorization").replace("Bearer ", "");
    }

    /**헤더 유효성 검상*/
    protected boolean tokenResolve(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        return header == null || !header.startsWith("Bearer ");
    }
}
