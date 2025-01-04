package kr.inuappcenterportal.inuportal.domain.member.fixture;

import kr.inuappcenterportal.inuportal.domain.member.dto.LoginDto;
import kr.inuappcenterportal.inuportal.domain.member.enums.Role;
import kr.inuappcenterportal.inuportal.domain.member.model.Member;

import java.util.Collections;
import java.util.List;

public enum MemberFixture {
    MEMBER_FIXTURE_1("20221212", "testNickName-1", Collections.singletonList(Role.USER.getAuthority())),
    MEMBER_FIXTURE_2("20231212", "testNickName-2", Collections.singletonList(Role.USER.getAuthority())),
    MEMBER_FIXTURE_3("20241212", "testNickName-3", Collections.singletonList(Role.USER.getAuthority()));

    private final String studentId;
    private final String nickName;
    private final List<String> role;

    MemberFixture(String studentId, String nickName, List<String> role) {
        this.studentId = studentId;
        this.nickName = nickName;
        this.role = role;
    }

    public Member create() {
        return new Member(studentId, nickName, role);
    }

    public LoginDto login() {
        return new LoginDto(studentId, "0000");
    }

    public String getStudentId() {
        return studentId;
    }

    public String getNickName() {
        return nickName;
    }
}
