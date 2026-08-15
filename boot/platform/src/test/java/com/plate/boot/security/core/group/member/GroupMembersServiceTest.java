package com.plate.boot.security.core.group.member;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.security.core.group.Group;
import com.plate.boot.security.core.group.GroupEvent;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GroupMembersService}, covering operate/save/delete plus the two
 * {@code @EventListener} handlers, without starting a Spring container or connecting to a database.
 */
@ExtendWith(MockitoExtension.class)
class GroupMembersServiceTest {

    @Mock
    private GroupMembersRepository memberRepository;

    @Mock
    private R2dbcEntityTemplate entityTemplate;

    @InjectMocks
    private GroupMembersService service;

    private CacheManager savedCacheManager;

    @BeforeEach
    void setUp() {
        savedCacheManager = ContextUtils.CACHE_MANAGER;
        ContextUtils.CACHE_MANAGER = null;
        service.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() {
        ContextUtils.APPLICATION_EVENT_PUBLISHER = null;
        ContextUtils.CACHE_MANAGER = savedCacheManager;
    }

    @Test
    void operateCreatesMemberWhenNotExists() {
        UUID groupCode = UUID.randomUUID();
        UUID userCode = UUID.randomUUID();
        GroupMemberReq request = new GroupMemberReq();
        request.setGroupCode(groupCode);
        request.setUserCode(userCode);

        R2dbcEntityTemplate saved = DatabaseUtils.ENTITY_TEMPLATE;
        DatabaseUtils.ENTITY_TEMPLATE = entityTemplate;
        try {
            when(entityTemplate.selectOne(any(Query.class), eq(GroupMember.class))).thenReturn(Mono.empty());
            when(memberRepository.save(any(GroupMember.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.operate(request))
                    .assertNext(member -> {
                        assertThat(member.getGroupCode()).isEqualTo(groupCode);
                        assertThat(member.getUserCode()).isEqualTo(userCode);
                    })
                    .verifyComplete();

            verify(memberRepository).save(any(GroupMember.class));
            verify(memberRepository, never()).findById(any(Long.class));
        } finally {
            DatabaseUtils.ENTITY_TEMPLATE = saved;
        }
    }

    @Test
    void operateUpdatesMemberWhenExists() {
        GroupMemberReq request = new GroupMemberReq();
        request.setGroupCode(UUID.randomUUID());
        request.setUserCode(UUID.randomUUID());

        GroupMember existing = new GroupMember();
        existing.setId(10L);
        existing.setGroupCode(request.getGroupCode());
        existing.setUserCode(request.getUserCode());

        R2dbcEntityTemplate saved = DatabaseUtils.ENTITY_TEMPLATE;
        DatabaseUtils.ENTITY_TEMPLATE = entityTemplate;
        try {
            when(entityTemplate.selectOne(any(Query.class), eq(GroupMember.class))).thenReturn(Mono.just(existing));
            when(memberRepository.findById(10L)).thenReturn(Mono.just(existing));
            when(memberRepository.save(any(GroupMember.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.operate(request))
                    .assertNext(member -> assertThat(member.getId()).isEqualTo(10L))
                    .verifyComplete();

            verify(memberRepository).findById(10L);
            verify(memberRepository).save(existing);
        } finally {
            DatabaseUtils.ENTITY_TEMPLATE = saved;
        }
    }

    @Test
    void saveNewMemberInserts() {
        GroupMember member = new GroupMember();
        member.setGroupCode(UUID.randomUUID());
        member.setUserCode(UUID.randomUUID());

        when(memberRepository.save(any(GroupMember.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.save(member))
                .assertNext(saved -> assertThat(saved).isSameAs(member))
                .verifyComplete();

        verify(memberRepository).save(member);
    }

    @Test
    void saveExistingMemberUpdates() {
        GroupMember member = new GroupMember();
        member.setId(3L);
        member.setGroupCode(UUID.randomUUID());
        member.setUserCode(UUID.randomUUID());

        when(memberRepository.findById(3L)).thenReturn(Mono.just(member));
        when(memberRepository.save(any(GroupMember.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.save(member))
                .assertNext(saved -> assertThat(saved).isSameAs(member))
                .verifyComplete();

        verify(memberRepository).findById(3L);
        verify(memberRepository).save(member);
    }

    @Test
    void saveExistingMemberCompletesEmptyWhenMissing() {
        GroupMember member = new GroupMember();
        member.setId(42L);

        when(memberRepository.findById(42L)).thenReturn(Mono.empty());

        StepVerifier.create(service.save(member)).verifyComplete();

        verify(memberRepository).findById(42L);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void deleteDeletesMember() {
        GroupMemberReq request = new GroupMemberReq();
        request.setId(1L);
        request.setGroupCode(UUID.randomUUID());
        request.setUserCode(UUID.randomUUID());

        when(memberRepository.delete(any(GroupMember.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(memberRepository).delete(any(GroupMember.class));
    }

    @Test
    void onUserDeletedEventRemovesMembersByUserCode() {
        UUID userCode = UUID.randomUUID();
        User user = new User();
        user.setCode(userCode);

        when(memberRepository.deleteByUserCode(userCode)).thenReturn(Mono.just(2));

        service.onUserDeletedEvent(UserEvent.delete(user));

        verify(memberRepository).deleteByUserCode(userCode);
    }

    @Test
    void onUserDeletedEventHandlesError() {
        UUID userCode = UUID.randomUUID();
        User user = new User();
        user.setCode(userCode);

        when(memberRepository.deleteByUserCode(userCode)).thenReturn(Mono.error(new RuntimeException("boom")));

        service.onUserDeletedEvent(UserEvent.delete(user));

        verify(memberRepository).deleteByUserCode(userCode);
    }

    @Test
    void onGroupDeletedEventRemovesMembersByGroupCode() {
        UUID groupCode = UUID.randomUUID();
        Group group = new Group();
        group.setCode(groupCode);

        when(memberRepository.deleteByGroupCode(groupCode)).thenReturn(Mono.just(3));

        service.onUserDeletedEvent(GroupEvent.delete(group));

        verify(memberRepository).deleteByGroupCode(groupCode);
    }

    @Test
    void searchReturnsMembers() {
        GroupMemberRes res = new GroupMemberRes();
        res.setId(1L);
        res.setName("alice");

        GroupMemberReq request = new GroupMemberReq();
        Pageable pageable = PageRequest.of(0, 10);

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), anyMap(), eq(GroupMemberRes.class)))
                    .thenReturn(Flux.just(res));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.search(request, pageable))
                    .expectNext(res)
                    .verifyComplete();
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }

    @Test
    void pageReturnsPagedMembers() {
        GroupMemberRes res = new GroupMemberRes();
        res.setId(1L);
        res.setName("alice");

        GroupMemberReq request = new GroupMemberReq();
        Pageable pageable = PageRequest.of(0, 10);

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), anyMap(), eq(GroupMemberRes.class)))
                    .thenReturn(Flux.just(res));
            db.when(() -> DatabaseUtils.count(anyString(), anyMap())).thenReturn(Mono.just(7L));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.page(request, pageable))
                    .assertNext(page -> {
                        assertThat(page.getContent()).containsExactly(res);
                        assertThat(page.getSize()).isEqualTo(pageable.getPageSize());
                        assertThat(page.getNumber()).isEqualTo(pageable.getPageNumber());
                        // countWithCache returns 0 on cache-miss, and PageImpl re-derives the
                        // total as offset + content.size() for this partial last page.
                        assertThat(page.getTotalElements()).isEqualTo(1L);
                    })
                    .verifyComplete();
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }
}
