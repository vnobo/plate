package com.plate.boot.security.core.group;

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
class GroupsServiceTest {

    @Mock
    private GroupsRepository groupsRepository;

    private GroupsService groupsService;

    private Group testGroup;

    private GroupReq testGroupReq;

    @BeforeEach
    void setUp() {
        groupsService = new GroupsService(groupsRepository);
        testGroup = new Group();
        testGroup.setCode(UUID.randomUUID());
        testGroup.setName("test_group");

        testGroupReq = new GroupReq();
        testGroupReq.setCode(testGroup.getCode());
        testGroupReq.setName("test_group_req");
    }

    @Nested
    @DisplayName("Group search and page tests")
    class GroupSearchTests {

        @Test
        @DisplayName("Should search groups successfully")
        void shouldSearchGroupsSuccessfully() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.query(any(String.class), any(Map.class), eq(Group.class)))
                        .thenReturn(Flux.just(testGroup));

                StepVerifier.create(groupsService.search(testGroupReq, PageRequest.of(0, 10)))
                        .expectNext(testGroup)
                        .verifyComplete();
            }
        }

        @Test
        @DisplayName("Should get page of groups successfully")
        void shouldGetPageOfGroupsSuccessfully() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.query(any(String.class), any(Map.class), eq(Group.class)))
                        .thenReturn(Flux.just(testGroup));
                mockedDatabaseUtils.when(() -> DatabaseUtils.count(any(String.class), any(Map.class)))
                        .thenReturn(Mono.just(1L));

                StepVerifier.create(groupsService.page(testGroupReq, PageRequest.of(0, 10)))
                        .expectNextMatches(page -> {
                            assertThat(page.getTotalElements()).isEqualTo(1);
                            assertThat(page.getContent()).contains(testGroup);
                            return true;
                        })
                        .verifyComplete();
            }
        }
    }

    @Nested
    @DisplayName("Group operation tests")
    class GroupOperationTests {

        @Test
        @DisplayName("Should operate on a group successfully")
        void shouldOperateOnGroupSuccessfully() {
            when(groupsRepository.findByCode(testGroupReq.getCode())).thenReturn(Mono.just(testGroup));
            when(groupsRepository.save(any(Group.class))).thenReturn(Mono.just(testGroup));

            StepVerifier.create(groupsService.operate(testGroupReq))
                    .expectNextMatches(group -> {
                        assertThat(group.getName()).isEqualTo(testGroupReq.getName());
                        return true;
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should delete a group successfully")
        void shouldDeleteGroupSuccessfully() {
            when(groupsRepository.delete(any(Group.class))).thenReturn(Mono.empty());

            StepVerifier.create(groupsService.delete(testGroupReq))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should save a new group successfully")
        void shouldSaveNewGroupSuccessfully() {
            Group newGroup = new Group();
            newGroup.setName("new_group");
            when(groupsRepository.save(newGroup)).thenReturn(Mono.just(newGroup));

            StepVerifier.create(groupsService.save(newGroup))
                    .expectNext(newGroup)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should update an existing group successfully")
        void shouldUpdateExistingGroupSuccessfully() {
            testGroup.setId(1); // This makes the entity "not new"
            when(groupsRepository.findById(1)).thenReturn(Mono.just(new Group()));
            when(groupsRepository.save(testGroup)).thenReturn(Mono.just(testGroup));

            StepVerifier.create(groupsService.save(testGroup))
                    .expectNext(testGroup)
                    .verifyComplete();
        }
    }
}