package com.plate.boot.security.core.group.authority;

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

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@ExtendWith(MockitoExtension.class)
class GroupAuthoritiesServiceTest {

    @Mock
    private GroupAuthoritiesRepository authoritiesRepository;

    private GroupAuthoritiesService authoritiesService;

    private GroupAuthority testGroupAuthority;

    private GroupAuthorityReq testGroupAuthorityReq;

    @BeforeEach
    void setUp() {
        authoritiesService = new GroupAuthoritiesService(authoritiesRepository);
        testGroupAuthority = new GroupAuthority();
        testGroupAuthority.setGroupCode(UUID.randomUUID());
        testGroupAuthority.setAuthority("ROLE_GROUP_USER");

        testGroupAuthorityReq = new GroupAuthorityReq();
        testGroupAuthorityReq.setGroupCode(testGroupAuthority.getGroupCode());
        testGroupAuthorityReq.setAuthority("ROLE_GROUP_ADMIN");
    }

    @Nested
    @DisplayName("Authority search tests")
    class AuthoritySearchTests {

        @Test
        @DisplayName("Should search authorities successfully")
        void shouldSearchAuthoritiesSuccessfully() {
            when(authoritiesRepository.findAll()).thenReturn(Flux.just(testGroupAuthority));

            StepVerifier.create(authoritiesService.search(testGroupAuthorityReq, PageRequest.of(0, 10)))
                    .expectNext(testGroupAuthority)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Authority operation tests")
    class AuthorityOperationTests {

        @Test
        @DisplayName("Should operate on an authority successfully")
        void shouldOperateOnAuthoritySuccessfully() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.ENTITY_TEMPLATE.selectOne(any(Query.class), eq(GroupAuthority.class)))
                        .thenReturn(Mono.just(testGroupAuthority));
                
                when(authoritiesRepository.save(any(GroupAuthority.class))).thenReturn(Mono.just(testGroupAuthority));

                StepVerifier.create(authoritiesService.operate(testGroupAuthorityReq))
                        .expectNext(testGroupAuthority)
                        .verifyComplete();
            }
        }

        @Test
        @DisplayName("Should create new authority when not found")
        void shouldCreateNewAuthorityWhenNotFound() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.ENTITY_TEMPLATE.selectOne(any(Query.class), eq(GroupAuthority.class)))
                        .thenReturn(Mono.empty());
                
                when(authoritiesRepository.save(any(GroupAuthority.class))).thenReturn(Mono.just(testGroupAuthority));

                StepVerifier.create(authoritiesService.operate(testGroupAuthorityReq))
                        .expectNext(testGroupAuthority)
                        .verifyComplete();
            }
        }

        @Test
        @DisplayName("Should delete an authority successfully")
        void shouldDeleteAuthoritySuccessfully() {
            when(authoritiesRepository.delete(any(GroupAuthority.class))).thenReturn(Mono.empty());

            StepVerifier.create(authoritiesService.delete(testGroupAuthorityReq))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Batch operation tests")
    class BatchOperationTests {

        @Test
        @DisplayName("Should batch update authorities successfully")
        void shouldBatchUpdateAuthoritiesSuccessfully() {
            testGroupAuthorityReq.setAuthorities(Set.of("ROLE_ADMIN", "ROLE_USER"));
            
            when(authoritiesRepository.findByGroupCode(testGroupAuthorityReq.getGroupCode()))
                    .thenReturn(Flux.just(testGroupAuthority));
            when(authoritiesRepository.saveAll(any(Iterable.class))).thenReturn(Flux.just(testGroupAuthority));
            when(authoritiesRepository.deleteAll(any(Iterable.class))).thenReturn(Mono.empty());

            StepVerifier.create(authoritiesService.batch(testGroupAuthorityReq))
                    .expectNext(200)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Authority save tests")
    class AuthoritySaveTests {

        @Test
        @DisplayName("Should save new authority")
        void shouldSaveNewAuthority() {
            // New authority has null id
            testGroupAuthority.setId(null);
            
            when(authoritiesRepository.save(testGroupAuthority)).thenReturn(Mono.just(testGroupAuthority));
            
            StepVerifier.create(authoritiesService.save(testGroupAuthority))
                    .expectNext(testGroupAuthority)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should update existing authority")
        void shouldUpdateExistingAuthority() {
            // Existing authority has non-null id
            testGroupAuthority.setId(1);
            
            when(authoritiesRepository.findById(1)).thenReturn(Mono.just(new GroupAuthority()));
            when(authoritiesRepository.save(testGroupAuthority)).thenReturn(Mono.just(testGroupAuthority));
            
            StepVerifier.create(authoritiesService.save(testGroupAuthority))
                    .expectNext(testGroupAuthority)
                    .verifyComplete();
        }
    }
}
