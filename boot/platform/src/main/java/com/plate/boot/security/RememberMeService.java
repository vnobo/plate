package com.plate.boot.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

/**
 * 「记住我」凭证：随机 token 存 Redis（2 天），浏览器只拿到一个 HttpOnly 的 remember-me cookie。
 * 前端既读不到也解不开它 —— 密码不再落盘，XSS 拿到的也只是个随时可吊销的随机串。
 */
@Service
@RequiredArgsConstructor
public class RememberMeService {

    public static final String COOKIE_NAME = "remember-me";
    private static final String KEY_PREFIX = "plate:remember-me:";
    private static final Duration TTL = Duration.ofDays(2);

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    /**
     * 登录成功且勾选了「记住我」时签发：token 存 Redis，cookie 里只放这个随机串。
     *
     * @param exchange 当前请求，用于写响应 cookie
     * @param username 登录成功的用户名
     * @return 写库与写 cookie 均完成后结束的 Mono
     */
    public Mono<Void> issue(@NonNull ServerWebExchange exchange, @NonNull String username) {
        String token = UUID.randomUUID().toString();
        return this.redisTemplate.opsForValue()
                .set(KEY_PREFIX + token, username, TTL)
                .then(Mono.fromRunnable(() -> exchange.getResponse().getCookies()
                        .add(COOKIE_NAME, buildCookie(exchange.getRequest(), token, TTL))));
    }

    /**
     * 把 cookie 里的 token 换回用户名；没有 cookie、已过期或已吊销则返回空。
     *
     * @param exchange 当前请求，用于读 remember-me cookie
     * @return 用户名，或空
     */
    public Mono<String> username(@NonNull ServerWebExchange exchange) {
        return Mono.justOrEmpty(cookieValue(exchange))
                .flatMap(token -> this.redisTemplate.opsForValue().get(KEY_PREFIX + token))
                .cast(String.class);
    }

    /**
     * 主动退出时吊销：删掉 Redis 里的 token，并让浏览器清掉 cookie（HttpOnly，前端删不掉）。
     *
     * @param exchange 当前请求，用于读 cookie 并写清除指令
     * @return 吊销完成后结束的 Mono
     */
    public Mono<Void> revoke(@NonNull ServerWebExchange exchange) {
        return Mono.justOrEmpty(cookieValue(exchange))
                .flatMap(token -> this.redisTemplate.opsForValue().delete(KEY_PREFIX + token))
                .then(Mono.fromRunnable(() -> exchange.getResponse().getCookies()
                        .add(COOKIE_NAME, buildCookie(exchange.getRequest(), "", Duration.ZERO))));
    }

    private String cookieValue(ServerWebExchange exchange) {
        var cookie = exchange.getRequest().getCookies().getFirst(COOKIE_NAME);
        return cookie == null ? null : cookie.getValue();
    }

    private ResponseCookie buildCookie(ServerHttpRequest request, String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .secure("https".equalsIgnoreCase(request.getURI().getScheme()))
                .build();
    }
}
