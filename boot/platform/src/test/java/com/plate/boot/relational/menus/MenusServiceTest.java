package com.plate.boot.relational.menus;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.config.InfrastructureConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringBootTest
@Import(InfrastructureConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MenusServiceTest {

    @Autowired
    private MenusService menusService;

    @Autowired
    private MenusRepository menusRepository;

    private Menu createMenu(String name, String authority) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setAuthority(authority);
        menu.setType(Menu.MenuType.MENU);
        menu.setPcode(UUID.randomUUID());
        return menu;
    }

    @BeforeEach
    void setUp() {
        menusRepository.deleteAll().block();
    }

    @Test
    @Order(1)
    void testSaveNewMenu() {
        Menu menu = createMenu("TestMenu", "test_menu");
        StepVerifier.create(menusService.save(menu))
                .assertNext(savedMenu -> {
                    Assertions.assertNotNull(savedMenu.getId());
                    Assertions.assertEquals("TestMenu", savedMenu.getName());
                    Assertions.assertEquals("ROLE_TEST_MENU", savedMenu.getAuthority());
                })
                .verifyComplete();
    }

    @Test
    @Order(2)
    void testSaveExistingMenu() {
        Menu menu = createMenu("TestMenu", "test_menu");
        Menu saved = menusService.save(menu).block();
        Assertions.assertNotNull(saved);

        saved.setName("Updated Menu");
        StepVerifier.create(menusService.save(saved))
                .assertNext(updatedMenu -> {
                    Assertions.assertEquals("Updated Menu", updatedMenu.getName());
                    Assertions.assertEquals(saved.getId(), updatedMenu.getId());
                })
                .verifyComplete();
    }

    @Test
    @Order(3)
    void testAddMenu() {
        MenuReq req = new MenuReq();
        req.setName("NewMenu");
        req.setAuthority("new_menu");
        req.setType(Menu.MenuType.MENU);
        req.setPcode(UUID.randomUUID());
        req.setTenantCode("0");
        StepVerifier.create(menusService.add(req))
                .assertNext(addedMenu -> {
                    Assertions.assertNotNull(addedMenu.getId());
                    Assertions.assertEquals("NewMenu", addedMenu.getName());
                })
                .verifyComplete();
    }

    @Test
    @Order(4)
    void testAddMenu_AlreadyExists() {
        menusService.save(createMenu("ExistingMenu", "existing_menu")).block();
        MenuReq req = new MenuReq();
        req.setName("ExistingMenu");
        req.setAuthority("existing_menu");
        req.setType(Menu.MenuType.MENU);
        req.setTenantCode("0");
        StepVerifier.create(menusService.add(req))
                .expectError(RestServerException.class)
                .verify();
    }

    @Test
    @Order(5)
    void testModifyMenu() {
        Menu saved = menusService.save(createMenu("ModifyMenu", "modify_menu")).block();
        Assertions.assertNotNull(saved);

        MenuReq req = new MenuReq();
        req.setCode(saved.getCode());
        req.setName("Modified Menu");

        StepVerifier.create(menusService.modify(req))
                .assertNext(modifiedMenu -> {
                    Assertions.assertEquals("Modified Menu", modifiedMenu.getName());
                    Assertions.assertEquals(saved.getId(), modifiedMenu.getId());
                })
                .verifyComplete();
    }

    @Test
    @Order(6)
    void testModifyMenu_NotFound() {
        MenuReq req = new MenuReq();
        req.setCode(UUID.randomUUID());
        req.setName("NonExistentMenu");
        StepVerifier.create(menusService.modify(req))
                .expectError(RestServerException.class)
                .verify();
    }

    @Test
    @Order(7)
    void testDelete() {
        Menu saved = menusService.save(createMenu("DeleteMenu", "delete_menu")).block();
        Assertions.assertNotNull(saved);

        MenuReq req = new MenuReq();
        req.setAuthority(saved.getAuthority());
        req.setTenantCode("0");

        StepVerifier.create(menusService.delete(req))
                .verifyComplete();

        StepVerifier.create(menusRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @Order(8)
    void testPage() {
        menusService.save(createMenu("Menu1", "menu_1")).block();
        menusService.save(createMenu("Menu2", "menu_2")).block();
        MenuReq req = new MenuReq();
        StepVerifier.create(menusService.page(req, PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(2, page.getTotalElements());
                    Assertions.assertEquals(2, page.getContent().size());
                })
                .verifyComplete();
    }

    @Test
    @Order(9)
    void testPage_Empty() {
        MenuReq req = new MenuReq();
        StepVerifier.create(menusService.page(req, PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(0, page.getTotalElements());
                    Assertions.assertTrue(page.getContent().isEmpty());
                })
                .verifyComplete();
    }
}