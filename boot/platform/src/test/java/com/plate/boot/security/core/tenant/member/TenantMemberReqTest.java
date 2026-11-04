package com.plate.boot.security.core.tenant.member;

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
 * Unit tests for {@link TenantMemberReq} (no Spring / container required).
 */
class TenantMemberReqTest {

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
        TenantMemberReq req = new TenantMemberReq();
        UUID userCode = UUID.randomUUID();
        Set<UUID> users = Set.of(userCode);

        req.setUserCode(userCode);
        req.setEnabled(true);
        req.setUsers(users);
        req.setUsername("alice");

        assertThat(req.getUserCode()).isEqualTo(userCode);
        assertThat(req.getEnabled()).isTrue();
        assertThat(req.getUsers()).isEqualTo(users);
        assertThat(req.getUsername()).isEqualTo("alice");
    }

    @Test
    void securityCodeSetsAndReturnsSelf() {
        TenantMemberReq req = new TenantMemberReq();
        UUID securityCode = UUID.randomUUID();

        TenantMemberReq result = req.securityCode(securityCode);

        assertThat(result).isSameAs(req);
        assertThat(req.getSecurityCode()).isEqualTo(securityCode);
    }

    @Test
    void toMemberTenantCopiesProperties() {
        TenantMemberReq req = new TenantMemberReq();
        UUID userCode = UUID.randomUUID();
        req.setUserCode(userCode);
        req.setEnabled(false);

        TenantMember member = req.toMemberTenant();

        assertThat(member.getUserCode()).isEqualTo(userCode);
        assertThat(member.getEnabled()).isFalse();
    }

    @Test
    void toCriteriaSkipsRequestOnlyFields() {
        TenantMemberReq req = new TenantMemberReq();
        req.setUserCode(UUID.randomUUID());

        Criteria criteria = req.toCriteria();

        assertThat(criteria).isNotNull();
    }

    @Test
    void toParamSqlBuildsJoinQueryWithUsersAndUsername() {
        TenantMemberReq req = new TenantMemberReq();
        UUID userCode = UUID.randomUUID();
        req.setUserCode(userCode);
        req.setUsers(Set.of(userCode));
        req.setUsername("alice");

        QueryFragment fragment = req.toParamSql();

        assertThat(fragment).isNotNull();
        String sql = fragment.querySql();
        assertThat(sql).contains("se_tenant_members a")
                .contains("inner join se_tenants b")
                .contains("inner join se_users c");
    }

    @Test
    void toParamSqlBuildsJoinQueryWithoutOptionalFilters() {
        TenantMemberReq req = new TenantMemberReq();
        req.setUserCode(UUID.randomUUID());

        QueryFragment fragment = req.toParamSql();

        assertThat(fragment).isNotNull();
        assertThat(fragment.querySql()).contains("se_tenant_members a");
    }

    @Test
    void equalsHashCodeAndToString() {
        TenantMemberReq a = new TenantMemberReq();
        a.setId(1L);
        TenantMemberReq b = new TenantMemberReq();
        b.setId(1L);
        TenantMemberReq c = new TenantMemberReq();
        c.setId(2L);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("TenantMemberReq");
    }
}
