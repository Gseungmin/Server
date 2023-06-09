package personal.project.web.filter.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import personal.project.domain.entity.Member;
import personal.project.exception.LoginException;
import personal.project.web.service.MemberService;
import personal.project.web.signature.SecuritySigner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static personal.project.domain.entity.MemberType.BASIC;
import static personal.project.domain.entity.MemberType.KAKAO;
import static personal.project.exception.ErrorType.*;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final HttpSecurity httpSecurity;
    private final SecuritySigner securitySigner;
    private final JWK jwk;
    private final MemberService memberService;

    /**인증 시작*/
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        Optional<Member> findMember = memberService.findMember(email);

        if (findMember.isPresent()) { //멤버가 존재할 경우
            if (findMember.get().getType().equals(KAKAO.getType())) { //베이직으로 로그인한 경우
                request.setAttribute("exception", KAKAO_MEMBER_EXIST);
                throw new LoginException(KAKAO_MEMBER_EXIST,
                        KAKAO_MEMBER_EXIST.getCode(), KAKAO_MEMBER_EXIST.getErrorMessage());
            }
        }

        AuthenticationManager authenticationManager = httpSecurity.getSharedObject(AuthenticationManager.class);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        return authentication;
    }

    /**인증에 성공시 즉 객체 조회 성공시 진행되는 로직, 토큰을 발행하는 코드 작성*/
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws ServletException, IOException {
        User user = (User) authResult.getPrincipal();
        String jwtToken;
        try {
            jwtToken = securitySigner.getJwtToken(user, jwk); //securitySigner를 통해 jwk 토큰을 받아옴
            response.addHeader("Authorization", "Bearer " + jwtToken); //발행받은 토큰을 response 헤더에 담아 응답
        } catch (JOSEException e) {
            e.printStackTrace();
        }
    }
}
