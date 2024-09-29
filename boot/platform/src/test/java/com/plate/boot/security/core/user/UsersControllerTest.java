package com.plate.boot.security.core.user;

import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.core.user.authority.UserAuthority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@ExtendWith(SpringExtension.class)
@WebFluxTest(UsersController.class)
class UsersControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UsersService usersService;

    @Test
    public void search_ValidRequest_ReturnsFluxOfUserResponses() {
        UserRequest request = new UserRequest();
        Pageable pageable = PageRequest.of(0, 10);

        List<UserResponse> responses = List.of(new UserResponse(), new UserResponse());

        when(ContextUtils.securityDetails()).thenReturn(Mono.just(new SecurityDetails(
                List.of(new UserAuthority("NONE_USER")),
                Map.of("username", "test"),
                "username")));
        when(usersService.search(any(), any(Pageable.class))).thenReturn(Flux.fromIterable(responses));

        webTestClient.get()
                .uri("/users/search?request={request}&pageable={pageable}", request, pageable)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class)
                .hasSize(2);
    }

    @Test
    public void page_ValidRequest_ReturnsPagedModelOfUserResponses() {
        UserRequest request = new UserRequest();
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponse> page = new PageImpl<>(List.of(new UserResponse(), new UserResponse()), pageable, 2);

        when(ContextUtils.securityDetails()).thenReturn(Mono.just(new SecurityDetails(
                List.of(new UserAuthority("NONE_USER")),
                Map.of("username", "test"),
                "username")));
        when(usersService.page(any(), any(Pageable.class))).thenReturn(Mono.just(page));

        webTestClient.get()
                .uri("/users/page?request={request}&pageable={pageable}", request, pageable)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedModel.class)
                .consumeWith(response -> {
                    PagedModel<UserResponse> pagedModel = response.getResponseBody();
                    assertNotNull(pagedModel);
                    assertEquals(2, pagedModel.getContent().size());
                });
    }

    @Test
    public void add_ValidRequest_ReturnsUserResponse() {
        UserRequest request = new UserRequest();

        User user = new User();
        UserResponse response = new UserResponse();

        when(usersService.add(any(UserRequest.class))).thenReturn(Mono.just(user));
        when(BeanUtils.copyProperties(any(User.class), eq(UserResponse.class))).thenReturn(response);

        webTestClient.post().uri("/users/add")
                .body(Mono.just(request), UserRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .isEqualTo(response);
    }

    @Test
    public void modify_ValidRequest_ReturnsUserResponse() {
        UserRequest request = new UserRequest();
        request.setId(1L);

        User user = new User();
        UserResponse response = new UserResponse();

        when(usersService.modify(any(UserRequest.class))).thenReturn(Mono.just(user));
        when(BeanUtils.copyProperties(any(User.class), eq(UserResponse.class))).thenReturn(response);

        webTestClient.put().uri("/users/modify")
                .body(Mono.just(request), UserRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .isEqualTo(response);
    }

    @Test
    public void delete_ValidRequest_ReturnsMonoVoid() {
        UserRequest request = new UserRequest();
        request.setId(1L);

        when(usersService.delete(any(UserRequest.class))).thenReturn(Mono.empty());

        webTestClient.method(HttpMethod.DELETE).uri("/users/delete")
                .body(Mono.just(request), UserRequest.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void add_InvalidRequest_ThrowsIllegalArgumentException() {
        UserRequest request = new UserRequest();
        request.setId(1L);  // Invalid because ID should be null for new user addition

        webTestClient.post().uri("/users/add")
                .body(Mono.just(request), UserRequest.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void modify_InvalidRequest_ThrowsIllegalArgumentException() {
        UserRequest request = new UserRequest();
        request.setId(null);  // Invalid because ID should not be null for modifying an existing user

        webTestClient.put().uri("/users/modify")
                .body(Mono.just(request), UserRequest.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void delete_InvalidRequest_ThrowsNullPointerException() {
        UserRequest request = new UserRequest();
        request.setId(null);  // Invalid because ID should not be null for deleting a user

        webTestClient.method(HttpMethod.DELETE).uri("/users/delete")
                .body(Mono.just(request), UserRequest.class)
                .exchange()
                .expectStatus().isBadRequest();
    }
}