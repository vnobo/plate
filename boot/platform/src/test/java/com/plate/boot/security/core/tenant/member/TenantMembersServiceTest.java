package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@ExtendWith(MockitoExtension.class)
class TenantMembersServiceTest {

    @Mock
    private TenantMembersRepository membersRepository;

    private TenantMembersService membersService;

    private TenantMember testTenantMember;

    private TenantMemberReq testTenantMemberReq;

    @BeforeEach
    void setUp() {
        membersService = new TenantMembersService(membersRepository);
        testTenantMember = new TenantMember();
        testTenantMember.setUserCode(UUID.randomUUID());
        testTenantMember.setTenantCode(UUID.randomUUID().toString());

        testTenantMemberReq = new TenantMemberReq();
        testTenantMemberReq.setUserCode(testTenantMember.getUserCode());
        testTenantMemberReq.setTenantCode(testTenantMember.getTenantCode());
    }

    @Nested
    @DisplayName("Member search and page tests")
    class MemberSearchTests {

        @Test
        @DisplayName("Should search members successfully")
        void shouldSearchMembersSuccessfully() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.query(any(String.class), any(Map.class), eq(TenantMemberRes.class)).blockFirst())
                        .thenReturn(Flux.just(new TenantMemberRes()));

                StepVerifier.create(membersService.search(testTenantMemberReq, PageRequest.of(0, 10)))
                        .expectNextCount(1)
                        .verifyComplete();
            }
        }

        @Test
        @DisplayName("Should get page of members successfully")
        void shouldGetPageOfMembersSuccessfully() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.query(any(String.class), any(Map.class), eq(TenantMemberRes.class)).blockFirst())
                        .thenReturn(Flux.just(new TenantMemberRes()));
                mockedDatabaseUtils.when(() -> DatabaseUtils.count(any(String.class), any(Map.class)).blockOptional())
                        .thenReturn(Mono.just(1L));

                StepVerifier.create(membersService.page(testTenantMemberReq, PageRequest.of(0, 10)))
                        .expectNextMatches(page -> {
                            assertThat(page.getTotalElements()).isEqualTo(1);
                            assertThat(page.getContent()).hasSize(1);
                            return true;
                        })
                        .verifyComplete();
            }
        }
    }

    @Nested
    @DisplayName("Member operation tests")
    class MemberOperationTests {

        @Test
        @DisplayName("Should operate on a member successfully")
        void shouldOperateOnMemberSuccessfully() {
            when(membersRepository.save(any(TenantMember.class))).thenReturn(Mono.just(testTenantMember));

            StepVerifier.create(membersService.operate(testTenantMemberReq))
                    .expectNextMatches(member -> {
                        assertThat(member.getUserCode()).isEqualTo(testTenantMemberReq.getUserCode());
                        return true;
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should delete a member successfully")
        void shouldDeleteMemberSuccessfully() {
            when(membersRepository.delete(any(TenantMember.class))).thenReturn(Mono.empty());

            StepVerifier.create(membersService.delete(testTenantMemberReq))
                    .verifyComplete();
        }
    }
}