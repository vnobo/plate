package com.plate.boot.security.core.group;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GroupReq} (no Spring / container required).
 */
class GroupReqTest {

    @Test
    void securityCodeSetsAndReturnsSelf() {
        GroupReq req = new GroupReq();
        UUID securityCode = UUID.randomUUID();

        GroupReq result = req.securityCode(securityCode);

        assertThat(result).isSameAs(req);
        assertThat(req.getSecurityCode()).isEqualTo(securityCode);
    }

    @Test
    void toGroupCopiesProperties() {
        GroupReq req = new GroupReq();
        req.setName("Managers");
        req.setDescription("Manager group");
        req.setPcode(UUID.randomUUID());

        Group group = req.toGroup();

        assertThat(group.getName()).isEqualTo("Managers");
        assertThat(group.getDescription()).isEqualTo("Manager group");
        assertThat(group.getPcode()).isEqualTo(req.getPcode());
    }

    @Test
    void equalsHashCodeAndToString() {
        GroupReq a = new GroupReq();
        a.setId(1);
        GroupReq b = new GroupReq();
        b.setId(1);
        GroupReq c = new GroupReq();
        c.setId(2);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("GroupReq");
    }
}
