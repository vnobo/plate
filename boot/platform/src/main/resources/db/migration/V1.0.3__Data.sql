insert into se_users(code, username, password, name, phone, email, bio, created_by, updated_by)
values ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'admin',
        '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
        'System Super Admin', '17089118266', null, null, 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479'),
       ('550e8400-e29b-41d4-a716-446655440000', 'user',
        '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
        'Normal User', null, null, 'Hello Bob,
Thanks for your interest in our Open Source Program.
If our collaboration looks promising, we’ll reach out to you with an email containing all the relevant information for partners.
Have a nice day!
Kind regards,JetBrains Community Support Team
www.jetbrains.com
The Drive to Develop', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479');

insert into se_authorities(code, user_code, authority, created_by, updated_by)
values ('1b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'ROLE_SYSTEM_ADMINISTRATORS',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479'),
       ('c9bf9e57-1685-4c89-badf-724a9e5d3a0e', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'ROLE_USER',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479'),
       ('3f2504e0-4f89-11d3-9a0c-0305e82c3301', '550e8400-e29b-41d4-a716-446655440000',
        'ROLE_USER',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479');

insert into se_groups(code, name, created_by, updated_by)
values ('d3d0e4d5-7f3b-4d75-94a6-2b6a8e6d7e9f', 'System Administrator',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479');

insert into se_group_members(code, group_code, user_code, created_by, updated_by)
values ('2c5f5b3a-1b7d-4a1f-9c1a-0e5a9e2d3c4b', 'd3d0e4d5-7f3b-4d75-94a6-2b6a8e6d7e9f',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479');

insert into se_group_authorities(code, group_code, authority, created_by, updated_by)
values ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd3d0e4d5-7f3b-4d75-94a6-2b6a8e6d7e9f',
        'ROLE_GROUP_ADMINISTRATORS',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479');


/********Init menus****************/
-- sort_no 决定 /menus/me 的返回顺序（MenusService 按 sortNo 升序），前端按此顺序平铺渲染
insert into se_menus(code, type, authority, name, path, sort_no, created_by, updated_by, extend)
values ('2f6b7c8d-9e0f-1a2b-3c4d-5e6f7a8b9c0d', 'MENU', 'ROLE_MENU_WELCOME', 'Home', 'welcome', 10,
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', '{
    "icons": "home"
  }');
insert into se_menus(code, type, authority, name, path, sort_no, created_by, updated_by, extend)
values ('7b8a9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', 'FOLDER', 'ROLE_FOLDER_SYSTEM', 'System Management', 'system', 20,
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', '{
    "icons": "setting"
  }');
insert into se_menus(code, pcode, type, authority, name, path, sort_no, created_by, updated_by, extend)
values ('4d5e6f70-8a9b-1c2d-3e4f-5a6b7c8d9e0f', '7b8a9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', 'MENU',
        'ROLE_MENU_SYSTEM_USERS', 'User Management', 'users', 30, 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', '{
    "icons": "user"
  }');
insert into se_menus(code, pcode, type, authority, name, path, sort_no, created_by, updated_by, extend)
values ('6a7b8c9d-0e1f-2a3b-4c5d-6e7f8a9b0c1d', '7b8a9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', 'MENU',
        'ROLE_MENU_SYSTEM_TENANTS', 'Tenant Management', 'tenant', 40, 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', '{
    "icons": "building"
  }');
insert into se_menus(code, pcode, type, authority, name, path, sort_no, created_by, updated_by, extend)
values ('1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d', '7b8a9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', 'MENU',
        'ROLE_MENU_SYSTEM_GROUPS', 'Role Management', 'roles', 50, 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', '{
    "icons": "group"
  }');
insert into se_menus(code, pcode, type, authority, name, path, sort_no, created_by, updated_by, extend)
values ('8e9f0a1b-2c3d-4e5f-6a7b-8c9d0e1f2a3b', '7b8a9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', 'MENU',
        'ROLE_MENU_SYSTEM_MENUS', 'Menu Management', 'menus', 60, 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', '{
    "icons": "menu"
  }');
