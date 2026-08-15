package com.plate.boot.security.core.group.member;

import com.plate.boot.commons.query.QueryFragment;
import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.core.TypeInformation;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.relational.core.query.Criteria;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GroupMemberReq} (no Spring / container required).
 */
class GroupMemberReqTest {

    private static R2dbcConverter savedConverter;

    @BeforeEach
    void setUpConverter() {
        savedConverter = DatabaseUtils.R2DBC_CONVERTER;
        R2dbcConverter stub = mock(R2dbcConverter.class);
        when(stub.writeValue(any(), any(TypeInformation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        DatabaseUtils.R2DBC_CONVERTER = stub;
    }

    @AfterEach
    void tearDownConverter() {
        DatabaseUtils.R2DBC_CONVERTER = savedConverter;
    }

    @Test
    void accessorsRoundTrip() {
        GroupMemberReq req = new GroupMemberReq();
        UUID groupCode = UUID.randomUUID();
        UUID userCode = UUID.randomUUID();
        Set<UUID> users = Set.of(userCode);

        req.setGroupCode(groupCode);
        req.setUserCode(userCode);
        req.setUsers(users);
        req.setUsername("alice");

        assertThat(req.getGroupCode()).isEqualTo(groupCode);
        assertThat(req.getUserCode()).isEqualTo(userCode);
        assertThat(req.getUsers()).isEqualTo(users);
        assertThat(req.getUsername()).isEqualTo("alice");
    }

    @Test
    void toGroupMemberCopiesProperties() {
        GroupMemberReq req = new GroupMemberReq();
        UUID groupCode = UUID.randomUUID();
        UUID userCode = UUID.randomUUID();
        req.setGroupCode(groupCode);
        req.setUserCode(userCode);

        GroupMember member = req.toGroupMember();

        assertThat(member.getGroupCode()).isEqualTo(groupCode);
        assertThat(member.getUserCode()).isEqualTo(userCode);
    }

    @Test
    void toCriteriaSkipsRequestOnlyFields() {
        GroupMemberReq req = new GroupMemberReq();
        req.setGroupCode(UUID.randomUUID());

        Criteria criteria = req.toCriteria();

        assertThat(criteria).isNotNull();
    }

    @Test
    void toParamSqlBuildsJoinQueryWithUsersAndUsername() {
        GroupMemberReq req = new GroupMemberReq();
        UUID groupCode = UUID.randomUUID();
        UUID userCode = UUID.randomUUID();
        req.setGroupCode(groupCode);
        req.setUsers(Set.of(userCode));
        req.setUsername("alice");

        QueryFragment fragment = req.toParamSql();

        assertThat(fragment).isNotNull();
        String sql = fragment.querySql();
        assertThat(sql).contains("se_group_members a")
                .contains("inner join se_groups b")
                .contains("inner join se_users c");
    }

    @Test
    void toParamSqlBuildsJoinQueryWithoutOptionalFilters() {
        GroupMemberReq req = new GroupMemberReq();
        req.setGroupCode(UUID.randomUUID());

        QueryFragment fragment = req.toParamSql();

        assertThat(fragment).isNotNull();
        assertThat(fragment.querySql()).contains("se_group_members a");
    }

    @Test
    void equalsHashCodeAndToString() {
        GroupMemberReq a = new GroupMemberReq();
        a.setId(1L);
        GroupMemberReq b = new GroupMemberReq();
        b.setId(1L);
        GroupMemberReq c = new GroupMemberReq();
        c.setId(2L);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("GroupMemberReq");
    }
}
