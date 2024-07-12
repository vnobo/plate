insert into se_users(code, username, password, name, creator, updater)
values ('U1000', 'admin',
        '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
        '系统超级管理员', 'U1000', 'U1000');

insert into se_authorities(code, user_code, authority, creator, updater)
values ('UA1000', 'U1000', 'ROLE_SYSTEM_ADMINISTRATORS', 'U1000', 'U1000'),
       ('UA1001', 'U1000', 'users:read', 'U1000', 'U1000'),
       ('UA1002', 'U1000', 'users:write', 'U1000', 'U1000'),
       ('UA1003', 'U1000', 'users:delete', 'U1000', 'U1000');

insert into se_groups(code, name, creator, updater)
values ('G1000', '系统管理员', 'U1000', 'U1000');

insert into se_group_members(code, group_code, user_code, creator, updater)
values ('GM1000', 'G1000', 'U1000', 'U1000', 'U1000');

insert into se_group_authorities(code, group_code, authority, creator, updater)
values ('GA1000', 'G1000', 'ROLE_ADMINISTRATORS', 'U1000', 'U1000'),
       ('GA1001', 'U1000', 'users:read', 'U1000', 'U1000'),
       ('GA1002', 'U1000', 'users:write', 'U1000', 'U1000');


/********Init menus****************/
insert into se_menus(code, type, authority, name, path, creator, updater, extend)
values ('M1000', 'FOLDER', 'ROLE_FOLDER_SYSTEM', '系统管理', 'system', 'U1000', 'U1000', '{
  "icons": "settings"
}');
insert into se_menus(code, pcode, type, authority, name, path, creator, updater, extend)
values ('M1001', 'M1000', 'MENU', 'ROLE_MENU_SYSTEM_USERS', '用户管理', 'users', 'U1000', 'U1000', '{
  "icons": "bi bi-people-fill"
}');
insert into se_menus(code, pcode, type, authority, name, path, creator, updater, extend)
values ('M1002', 'M1000', 'MENU', 'ROLE_MENU_SYSTEM_GROUPS', '角色管理', 'roles', 'U1000', 'U1000', '{
  "icons": "bi bi-collection-fill"
}');
insert into se_menus(code, pcode, type, authority, name, path, creator, updater, extend)
values ('M1003', 'M1000', 'MENU', 'ROLE_MENU_SYSTEM_MENUS', '菜单管理', 'menus', 'U1000', 'U1000', '{
  "icons": "bi bi-menu-button-wide-fill"
}');