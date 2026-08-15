package com.plate.boot.security.core.group.member;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GroupMembersController}.
 */
@ExtendWith(MockitoExtension.class)
class GroupMembersControllerTest {

    @Mock
    private GroupMembersService groupMembersService;

    @InjectMocks
    private GroupMembersController controller;

    private static GroupMemberRes memberRes() {
        GroupMemberRes res = new GroupMemberRes();
        res.setName("ops");
        return res;
    }

    private static GroupMember member() {
        GroupMember member = new GroupMember();
        member.setGroupCode(java.util.UUID.randomUUID());
        member.setUserCode(java.util.UUID.randomUUID());
        return member;
    }

    @Test
    void searchDelegatesToService() {
        GroupMemberReq request = new GroupMemberReq();
        Pageable pageable = PageRequest.of(0, 20);
        GroupMemberRes res = memberRes();
        when(groupMembersService.search(request, pageable)).thenReturn(Flux.just(res));

        StepVerifier.create(controller.search(request, pageable))
                .expectNext(res)
                .verifyComplete();

        verify(groupMembersService).search(request, pageable);
    }

    @Test
    void pageWrapsServicePageInPagedModel() {
        GroupMemberReq request = new GroupMemberReq();
        Pageable pageable = PageRequest.of(0, 20);
        GroupMemberRes res = memberRes();
        Page<GroupMemberRes> page = new PageImpl<>(List.of(res), pageable, 1);
        when(groupMembersService.page(request, pageable)).thenReturn(Mono.just(page));

        StepVerifier.create(controller.page(request, pageable))
                .assertNext(paged -> {
                    assertThat(paged).isInstanceOf(PagedModel.class);
                    assertThat(paged.getContent()).containsExactly(res);
                })
                .verifyComplete();

        verify(groupMembersService).page(request, pageable);
    }

    @Test
    void saveDelegatesToService() {
        GroupMemberReq request = new GroupMemberReq();
        GroupMember member = member();
        when(groupMembersService.operate(request)).thenReturn(Mono.just(member));

        StepVerifier.create(controller.save(request))
                .expectNext(member)
                .verifyComplete();

        verify(groupMembersService).operate(request);
    }

    @Test
    void saveBatchEmitsProgressEvents() {
        GroupMemberReq first = new GroupMemberReq();
        GroupMemberReq second = new GroupMemberReq();
        when(groupMembersService.operate(any(GroupMemberReq.class))).thenReturn(Mono.just(member()));

        StepVerifier.create(controller.saveBatch(Flux.just(first, second)))
                .assertNext(event -> assertThat(event.getProcessed()).isZero())
                .assertNext(event -> assertThat(event.getProcessed()).isEqualTo(1L))
                .assertNext(event -> assertThat(event.getProcessed()).isEqualTo(2L))
                .assertNext(event -> assertThat(event.getProcessed()).isEqualTo(100L))
                .verifyComplete();

        verify(groupMembersService, times(2)).operate(any(GroupMemberReq.class));
    }

    @Test
    void deleteRejectsNullId() {
        GroupMemberReq request = new GroupMemberReq();

        assertThatThrownBy(() -> controller.delete(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID must not be null");

        verify(groupMembersService, never()).delete(any(GroupMemberReq.class));
    }

    @Test
    void deleteDelegatesWhenIdPresent() {
        GroupMemberReq request = new GroupMemberReq();
        request.setId(1L);

        when(groupMembersService.delete(request)).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(request)).verifyComplete();

        verify(groupMembersService).delete(request);
    }
}
