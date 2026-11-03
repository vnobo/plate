package com.plate.boot.security.jwt;

/**
 * Enumeration of the client platforms supported by the JWT authentication endpoints.
 * <p>
 * Each authentication request declares which kind of client it originates from. The value is
 * embedded into the issued JWT claims so that tokens are traceable to their issuing channel
 * and per-channel policy decisions (TTL, refresh behaviour) can be applied later.
 * </p>
 *
 * <ul>
 *   <li>{@link #PC} — browser / desktop web clients (SPA or server-rendered pages).</li>
 *   <li>{@link #MINIAPP} — WeChat mini-program clients authenticating via {@code wx.login} code exchange.</li>
 *   <li>{@link #APP} — native mobile applications authenticating with username and password.</li>
 * </ul>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public enum ClientType {

    /**
     * Browser or desktop web client.
     */
    PC,

    /**
     * WeChat mini-program client.
     */
    MINIAPP,

    /**
     * Native mobile application client.
     */
    APP
}
