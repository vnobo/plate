insert into se_users(code, username, password, name, extend)
values ('U1000', 'admin', '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
        '系统超级管理员', '{}');
insert into se_authorities(code, user_code, authority)
values ('A1000', 'U1000', 'ROLE_ADMINISTRATORS');

insert into se_groups(code, name)
values ('G1000', '默认组');

insert into se_group_members(code, group_code, user_code)
values ('GM1000', 'G1000', 'U1000');

insert into se_group_authorities(code, group_code, authority)
values ('GA1000', 'G1000', 'ROLE_GROUPS_ADMINISTRATORS');

insert into se_tenants(pcode, code, name)
values ('0', '610115', '默认租户');

insert into se_tenant_members(code, tenant_code, user_code)
values ('TM1000', '610115', 'U1000');

insert into se_menus(code, type, authority, name, path, extend)
values ('1000', 'FOLDER', 'ROLE_FOLDER_SYSTEM', 'System manager', '', '{
  "icons": "settings"
}');

insert into se_menus(code, pcode, type, authority, name, path, extend)
values ('1001', '1000', 'MENU', 'ROLE_MENU_SYSTEM_MENUS', 'MENUS', '/system/menus', '{
  "icons": "menu-2"
}');