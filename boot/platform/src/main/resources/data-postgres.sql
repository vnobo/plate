insert into se_users(code, username, password, name, phone, email, bio, creator, updater)
values ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'admin',
        '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
        '系统超级管理员', '17089118266', null, null, 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479'),
       ('6ba7b810-9dad-11d1-80b4-00c04fd430c8', 'farmer',
        '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
        '测试用户', null, '5199840@qq.com', null, 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479'),
       ('550e8400-e29b-41d4-a716-446655440000', 'user',
        '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
        '普通用户', null, null, 'PostgreSQL Elephant Logo Home About 我们可以创建一个GIN索引（第12.9节）来加快文本搜索：
另一种方法是创建一个单独的tsvector列来保存to_tsvector的输出。要使此列自动更新其源数据，请使用存储的生成列。此示例是title和body的串联，使用coalesce来确保当另一个字段为NULL时仍将索引一个字段：
', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479');

insert into se_authorities(code, user_code, authority, creator, updater)
values ('1b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'ROLE_SYSTEM_ADMINISTRATORS',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479'),
       ('c9bf9e57-1685-4c89-badf-724a9e5d3a0e', '6ba7b810-9dad-11d1-80b4-00c04fd430c8', 'ROLE_USER',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479'),
       ('3f2504e0-4f89-11d3-9a0c-0305e82c3301', '550e8400-e29b-41d4-a716-446655440000', 'ROLE_USER',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479');

insert into se_groups(code, name, creator, updater)
values ('d3d0e4d5-7f3b-4d75-94a6-2b6a8e6d7e9f', '系统管理员', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479');

insert into se_group_members(code, group_code, user_code, creator, updater)
values ('2c5f5b3a-1b7d-4a1f-9c1a-0e5a9e2d3c4b', 'd3d0e4d5-7f3b-4d75-94a6-2b6a8e6d7e9f',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479');

insert into se_group_authorities(code, group_code, authority, creator, updater)
values ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd3d0e4d5-7f3b-4d75-94a6-2b6a8e6d7e9f', 'ROLE_ADMINISTRATORS',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479'),
       ('e4d3f2a1-5c6d-4e7f-8a9b-3d1c2e3f4a5b', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'users:read',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479'),
       ('9c4d6b8a-2e3c-4d5e-8f7a-1b9a8c7d6e5f', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'users:write',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479');


/********Init menus****************/
insert into se_menus(code, type, authority, name, path, creator, updater, extend)
values ('7b8a9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', 'FOLDER', 'ROLE_FOLDER_SYSTEM', '系统管理', 'system',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', '{
  "icons": "setting"
}');
insert into se_menus(code, pcode, type, authority, name, path, creator, updater, extend)
values ('4d5e6f70-8a9b-1c2d-3e4f-5a6b7c8d9e0f', '7b8a9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', 'MENU',
        'ROLE_MENU_SYSTEM_USERS', '用户管理', 'users', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', '{
  "icons": "user"
}');
insert into se_menus(code, pcode, type, authority, name, path, creator, updater, extend)
values ('1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d', '7b8a9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', 'MENU',
        'ROLE_MENU_SYSTEM_GROUPS', '角色管理', 'roles', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', '{
  "icons": "group"
}');
insert into se_menus(code, pcode, type, authority, name, path, creator, updater, extend)
values ('8e9f0a1b-2c3d-4e5f-6a7b-8c9d0e1f2a3b', '7b8a9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', 'MENU',
        'ROLE_MENU_SYSTEM_MENUS', '菜单管理', 'menus', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', '{
  "icons": "menu"
}');