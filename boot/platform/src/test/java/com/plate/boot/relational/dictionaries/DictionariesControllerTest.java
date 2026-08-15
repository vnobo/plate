package com.plate.boot.relational.dictionaries;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DictionariesController}.
 */
@ExtendWith(MockitoExtension.class)
class DictionariesControllerTest {

    @Mock
    private DictionariesService dictionariesService;

    @InjectMocks
    private DictionariesController controller;

    private static Dictionary dictionary(String dictType, String dictKey) {
        Dictionary dictionary = new Dictionary();
        dictionary.setDictType(dictType);
        dictionary.setDictKey(dictKey);
        return dictionary;
    }

    @Test
    void searchReplacesUnpagedPageableWithDefault() {
        DictionaryReq request = new DictionaryReq();
        Dictionary item = dictionary("USER_STATUS", "ACTIVE");
        Page<Dictionary> page = new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);

        when(dictionariesService.page(any(DictionaryReq.class), any(Pageable.class))).thenReturn(Mono.just(page));

        StepVerifier.create(controller.search(request, Pageable.unpaged()))
                .expectNext(page)
                .verifyComplete();

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(dictionariesService).page(eq(request), captor.capture());
        assertThat(captor.getValue().isPaged()).isTrue();
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void searchKeepsPagedPageable() {
        DictionaryReq request = new DictionaryReq();
        Pageable pageable = PageRequest.of(2, 15);
        Dictionary item = dictionary("USER_STATUS", "ACTIVE");
        Page<Dictionary> page = new PageImpl<>(List.of(item), pageable, 1);

        when(dictionariesService.page(request, pageable)).thenReturn(Mono.just(page));

        StepVerifier.create(controller.search(request, pageable))
                .expectNext(page)
                .verifyComplete();

        verify(dictionariesService).page(request, pageable);
    }

    @Test
    void findByTypeDelegatesWhenEnabledIsNull() {
        UUID tenantCode = UUID.randomUUID();
        Dictionary item = dictionary("USER_STATUS", "ACTIVE");
        when(dictionariesService.findByType(tenantCode, "USER_STATUS")).thenReturn(Flux.just(item));

        StepVerifier.create(controller.findByType(tenantCode, "USER_STATUS", null))
                .expectNext(item)
                .verifyComplete();

        verify(dictionariesService).findByType(tenantCode, "USER_STATUS");
        verify(dictionariesService, never()).findEnabledByType(any(), any());
    }

    @Test
    void findByTypeDelegatesWhenEnabledIsFalse() {
        UUID tenantCode = UUID.randomUUID();
        Dictionary item = dictionary("USER_STATUS", "ACTIVE");
        when(dictionariesService.findByType(tenantCode, "USER_STATUS")).thenReturn(Flux.just(item));

        StepVerifier.create(controller.findByType(tenantCode, "USER_STATUS", false))
                .expectNext(item)
                .verifyComplete();

        verify(dictionariesService).findByType(tenantCode, "USER_STATUS");
        verify(dictionariesService, never()).findEnabledByType(any(), any());
    }

    @Test
    void findByTypeUsesEnabledLookupWhenEnabledIsTrue() {
        UUID tenantCode = UUID.randomUUID();
        Dictionary item = dictionary("USER_STATUS", "ACTIVE");
        when(dictionariesService.findEnabledByType(tenantCode, "USER_STATUS")).thenReturn(Flux.just(item));

        StepVerifier.create(controller.findByType(tenantCode, "USER_STATUS", true))
                .expectNext(item)
                .verifyComplete();

        verify(dictionariesService).findEnabledByType(tenantCode, "USER_STATUS");
        verify(dictionariesService, never()).findByType(any(), any());
    }

    @Test
    void findChildrenDelegatesToService() {
        UUID pcode = UUID.randomUUID();
        Dictionary child = dictionary("USER_STATUS", "ACTIVE");
        when(dictionariesService.findChildren(pcode)).thenReturn(Flux.just(child));

        StepVerifier.create(controller.findChildren(pcode))
                .expectNext(child)
                .verifyComplete();

        verify(dictionariesService).findChildren(pcode);
    }

    @Test
    void addDelegatesToService() {
        DictionaryReq request = new DictionaryReq();
        request.setDictValue("1");
        request.setDictLabel("Active");

        Dictionary saved = dictionary("USER_STATUS", "ACTIVE");
        when(dictionariesService.add(request)).thenReturn(Mono.just(saved));

        StepVerifier.create(controller.add(request))
                .expectNext(saved)
                .verifyComplete();

        verify(dictionariesService).add(request);
    }

    @Test
    void modifyRejectsNullCode() {
        DictionaryReq request = new DictionaryReq();

        StepVerifier.create(controller.modify(request))
                .expectErrorSatisfies(err ->
                        assertThat(err).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Dictionary code is required"))
                .verify();

        verify(dictionariesService, never()).modify(any(DictionaryReq.class));
    }

    @Test
    void modifyDelegatesWhenCodePresent() {
        DictionaryReq request = new DictionaryReq();
        request.setCode(UUID.randomUUID());

        Dictionary saved = dictionary("USER_STATUS", "ACTIVE");
        when(dictionariesService.modify(request)).thenReturn(Mono.just(saved));

        StepVerifier.create(controller.modify(request))
                .expectNext(saved)
                .verifyComplete();

        verify(dictionariesService).modify(request);
    }

    @Test
    void deleteRejectsNullCode() {
        DictionaryReq request = new DictionaryReq();

        StepVerifier.create(controller.delete(request))
                .expectErrorSatisfies(err ->
                        assertThat(err).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Dictionary code is required"))
                .verify();

        verify(dictionariesService, never()).delete(any(DictionaryReq.class));
    }

    @Test
    void deleteDelegatesWhenCodePresent() {
        DictionaryReq request = new DictionaryReq();
        request.setCode(UUID.randomUUID());

        when(dictionariesService.delete(request)).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(request)).verifyComplete();

        verify(dictionariesService).delete(request);
    }

    @Test
    void batchAddFlattensEachDictionary() {
        DictionaryReq first = new DictionaryReq();
        first.setDictKey("ACTIVE");
        DictionaryReq second = new DictionaryReq();
        second.setDictKey("INACTIVE");

        Dictionary savedFirst = dictionary("STATUS", "ACTIVE");
        Dictionary savedSecond = dictionary("STATUS", "INACTIVE");
        when(dictionariesService.add(first)).thenReturn(Mono.just(savedFirst));
        when(dictionariesService.add(second)).thenReturn(Mono.just(savedSecond));

        StepVerifier.create(controller.batchAdd(new DictionaryReq[]{first, second}))
                .expectNext(savedFirst)
                .expectNext(savedSecond)
                .verifyComplete();

        verify(dictionariesService).add(first);
        verify(dictionariesService).add(second);
    }
}
