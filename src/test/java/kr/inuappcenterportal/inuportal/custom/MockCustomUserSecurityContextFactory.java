package kr.inuappcenterportal.inuportal.custom;

import kr.inuappcenterportal.inuportal.domain.member.model.Member;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class MockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustom> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustom customMember) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Mock Member 객체 생성
        Member mockMember = Mockito.mock(Member.class);
        Mockito.when(mockMember.getId()).thenReturn(customMember.id());
        Mockito.when(mockMember.getUsername()).thenReturn(customMember.schoolId());

        // Authentication 생성
        UsernamePasswordAuthenticationToken  authentication= new UsernamePasswordAuthenticationToken(mockMember, "", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // SecurityContext에 Authentication 설정
        context.setAuthentication(authentication);
        return context;
    }
}
