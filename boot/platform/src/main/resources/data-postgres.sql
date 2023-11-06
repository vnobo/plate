insert into se_users(code, username, password, name, creator, updater, extend)
values ('U1000', 'admin', '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
        '系统超级管理员', 'U1000', 'U1000', '{}');
insert into se_authorities(code, user_code, authority, creator, updater)
values ('A1000', 'U1000', 'ROLE_ADMINISTRATORS', 'U1000', 'U1000');

insert into se_groups(code, name, creator, updater)
values ('G1000', '默认组', 'U1000', 'U1000');

insert into se_group_members(code, group_code, user_code, creator, updater)
values ('GM1000', 'G1000', 'U1000', 'U1000', 'U1000');

insert into se_group_authorities(code, group_code, authority, creator, updater)
values ('GA1000', 'G1000', 'ROLE_GROUPS_ADMINISTRATORS', 'U1000', 'U1000');

insert into se_tenants(code, name, creator, updater)
values ('610115', '默认租户', 'U1000', 'U1000');

insert into se_tenant_members(code, tenant_code, user_code, creator, updater)
values ('TM1000', '610115', 'U1000', 'U1000', 'U1000');

insert into se_menus(code, type, authority, name, path, creator, updater, extend)
values ('1000', 'FOLDER', 'ROLE_FOLDER_SYSTEM', '系统管理', '', 'U1000', 'U1000', '{
  "icons": "settings"
}');

insert into se_menus(code, pcode, type, authority, name, path, creator, updater, extend)
values ('1001', '1000', 'MENU', 'ROLE_MENU_SYSTEM_MENUS', '菜单管理', '/system/menus', 'U1000', 'U1000', '{
  "icons": "menu-2"
}');