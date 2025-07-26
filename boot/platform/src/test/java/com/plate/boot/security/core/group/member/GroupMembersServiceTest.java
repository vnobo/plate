package com.plate.boot.security.core.group.member;

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
import org.springframework.data.relational.core.query.Query;
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
class GroupMembersServiceTest {

    @Mock
    private GroupMembersRepository membersRepository;

    private GroupMembersService membersService;

    private GroupMember testGroupMember;

    private GroupMemberReq testGroupMemberReq;

    @BeforeEach
    void setUp() {
        membersService = new GroupMembersService(membersRepository);
        testGroupMember = new GroupMember();
        testGroupMember.setUserCode(UUID.randomUUID());
        testGroupMember.setGroupCode(UUID.randomUUID());

        testGroupMemberReq = new GroupMemberReq();
        testGroupMemberReq.setUserCode(testGroupMember.getUserCode());
        testGroupMemberReq.setGroupCode(testGroupMember.getGroupCode());
    }

    @Nested
    @DisplayName("Member search and page tests")
    class MemberSearchTests {

        @Test
        @DisplayName("Should search members successfully")
        void shouldSearchMembersSuccessfully() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.query(any(String.class), any(Map.class), eq(GroupMemberRes.class)))
                        .thenReturn(Flux.just(new GroupMemberRes()));

                StepVerifier.create(membersService.search(testGroupMemberReq, PageRequest.of(0, 10)))
                        .expectNextCount(1)
                        .verifyComplete();
            }
        }

        @Test
        @DisplayName("Should get page of members successfully")
        void shouldGetPageOfMembersSuccessfully() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.query(any(String.class), any(Map.class), eq(GroupMemberRes.class)))
                        .thenReturn(Flux.just(new GroupMemberRes()));
                mockedDatabaseUtils.when(() -> DatabaseUtils.count(any(String.class), any(Map.class)))
                        .thenReturn(Mono.just(1L));

                StepVerifier.create(membersService.page(testGroupMemberReq, PageRequest.of(0, 10)))
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
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.ENTITY_TEMPLATE.selectOne(any(Query.class), eq(GroupMember.class)))
                        .thenReturn(Mono.just(testGroupMember));
                
                when(membersRepository.save(any(GroupMember.class))).thenReturn(Mono.just(testGroupMember));

                StepVerifier.create(membersService.operate(testGroupMemberReq))
                        .expectNext(testGroupMember)
                        .verifyComplete();
            }
        }

        @Test
        @DisplayName("Should delete a member successfully")
        void shouldDeleteMemberSuccessfully() {
            when(membersRepository.delete(any(GroupMember.class))).thenReturn(Mono.empty());

            StepVerifier.create(membersService.delete(testGroupMemberReq))
                    .verifyComplete();
        }
    }
}