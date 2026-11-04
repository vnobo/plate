package com.plate.boot.security.core;

import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UsersRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserAuditorAware} (no Spring / R2DBC connection required).
 * <p>
 * The {@link UsersRepository} and {@link CacheManager} collaborators are mocked. Cache-backed
 * branches are exercised via a mocked {@link Cache} so no Redis/ConcurrentMap cache instance is needed.
 */
class UserAuditorAwareTest {

    @Test
    void constructorClearsCacheWhenPresent() {
        UsersRepository repository = mock(UsersRepository.class);
        CacheManager cacheManager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.getNativeCache()).thenReturn(new ConcurrentHashMap<>());
        when(cache.getName()).thenReturn("test.cache");

        new UserAuditorAware(repository, cacheManager);

        verify(cache).clear();
    }

    @Test
    void getCurrentAuditorMapsSecurityDetails() {
        UserAuditorAware service = new UserAuditorAware(mock(UsersRepository.class), mock(CacheManager.class));

        UUID code = UUID.randomUUID();
        SecurityDetails details = new SecurityDetails(List.of(), Map.of("username", "admin"), "username");
        details.setCode(code);
        details.setUsername("admin");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(details);

        Mono<UserAuditor> auditor = service.getCurrentAuditor()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                        Mono.just(new SecurityContextImpl(authentication))));

        StepVerifier.create(auditor)
                .assertNext(result -> {
                    assertThat(result.code()).isEqualTo(code);
                    assertThat(result.name()).isEqualTo("admin");
                })
                .verifyComplete();
    }

    @Test
    void getCurrentAuditorReturnsEmptyWithoutSecurityContext() {
        UserAuditorAware service = new UserAuditorAware(mock(UsersRepository.class), mock(CacheManager.class));

        StepVerifier.create(service.getCurrentAuditor()).verifyComplete();
    }

    @Test
    void loadByCodeReturnsEmptyWhenCodeIsNull() {
        UsersRepository repository = mock(UsersRepository.class);
        UserAuditorAware service = new UserAuditorAware(repository, mock(CacheManager.class));

        StepVerifier.create(service.loadByCode(null)).verifyComplete();

        verify(repository, never()).findByCode(any());
    }

    @Test
    void loadByCodeQueriesRepositoryWhenCacheIsMissing() {
        UsersRepository repository = mock(UsersRepository.class);
        CacheManager cacheManager = mock(CacheManager.class);
        when(cacheManager.getCache(anyString())).thenReturn(null);
        UserAuditorAware service = new UserAuditorAware(repository, cacheManager);

        UUID code = UUID.randomUUID();
        User user = new User();
        user.setCode(code);
        user.setName("alice");
        when(repository.findByCode(code)).thenReturn(Mono.just(user));

        StepVerifier.create(service.loadByCode(code))
                .assertNext(auditor -> {
                    assertThat(auditor.code()).isEqualTo(code);
                    assertThat(auditor.name()).isEqualTo("alice");
                })
                .verifyComplete();
    }

    @Test
    void loadByCodeReturnsCachedAuditorWhenPresent() {
        UsersRepository repository = mock(UsersRepository.class);
        CacheManager cacheManager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.getNativeCache()).thenReturn(new ConcurrentHashMap<>());
        when(cache.getName()).thenReturn("test.cache");
        UserAuditorAware service = new UserAuditorAware(repository, cacheManager);

        UUID code = UUID.randomUUID();
        UserAuditor cached = UserAuditor.of(code, "cached");
        when(cache.get(eq(code), ArgumentMatchers.<Callable<UserAuditor>>any())).thenReturn(cached);

        StepVerifier.create(service.loadByCode(code))
                .assertNext(auditor -> assertThat(auditor).isEqualTo(cached))
                .verifyComplete();

        verify(cache, never()).put(any(), any());
        verify(repository, never()).findByCode(any());
    }

    @Test
    void loadByCodeFallsBackToRepositoryOnCacheMiss() {
        UsersRepository repository = mock(UsersRepository.class);
        CacheManager cacheManager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.getNativeCache()).thenReturn(new ConcurrentHashMap<>());
        when(cache.getName()).thenReturn("test.cache");
        UserAuditorAware service = new UserAuditorAware(repository, cacheManager);

        UUID code = UUID.randomUUID();
        User user = new User();
        user.setCode(code);
        user.setName("bob");
        when(cache.get(eq(code), ArgumentMatchers.<Callable<UserAuditor>>any())).thenReturn(null);
        when(repository.findByCode(code)).thenReturn(Mono.just(user));

        StepVerifier.create(service.loadByCode(code))
                .assertNext(auditor -> assertThat(auditor.code()).isEqualTo(code))
                .verifyComplete();

        verify(cache).put(eq(code), any(UserAuditor.class));
    }
}
