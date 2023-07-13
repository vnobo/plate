insert into se_users(username, password)
values ('admin', '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa');
insert into se_authorities(username, authority)
values ('admin', 'ROLE_ADMINISTRATORS');
values ('admin', 'ROLE_GROUP_ADMINISTRATORS');

insert into se_menus(code, type, authority, name, path, extend)
values ('1000', 'FOLDER', 'ROLE_FOLDER_SYSTEM', 'System manager', '', '{
  "icons": "settings"
}');
insert into se_menus(code, pcode, type, authority, name, path, extend)
values ('1001', '1000', 'MENU', 'ROLE_MENU_SYSTEM_MENUS', 'MENUS', '/system/menus', '{
  "icons": "menu-2"
}');
insert into se_menus(code, pcode, type, authority, name, path, extend)
values ('1002', '1000', 'MENU', 'ROLE_MENU_SYSTEM_USERS', 'USERS', '/system/users', '{
  "icons": "users",
  "permissions": [
    {
      "method": "GET",
      "name": "User Search",
      "path": "/users/v1/search,/users/v1/page",
      "authority": "ROLE_API_SYSTEM_USERS_READ"
    },
    {
      "method": "POST",
      "name": "User Add",
      "path": "/users/v1/add",
      "authority": "ROLE_API_SYSTEM_USERS_ADD"
    },
    {
      "method": "PUT",
      "name": "User Modify",
      "path": "/users/v1/modify",
      "authority": "ROLE_API_SYSTEM_USERS_MODIFY"
    },
    {
      "method": "DELETE",
      "name": "User Delete",
      "path": "/users/v1/delete",
      "authority": "ROLE_API_SYSTEM_USERS_DELETE"
    }
  ]
}')