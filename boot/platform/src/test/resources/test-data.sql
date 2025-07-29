-- 测试数据初始化脚本
-- 用于 SecurityController 集成测试

-- 清理现有测试数据
DELETE FROM se_group_authorities WHERE group_code IN (SELECT code FROM se_groups WHERE name LIKE '%测试%');
DELETE FROM se_group_members WHERE group_code IN (SELECT code FROM se_groups WHERE name LIKE '%测试%');
DELETE FROM se_groups WHERE name LIKE '%测试%';
DELETE FROM se_authorities WHERE user_code IN (SELECT code FROM se_users WHERE username IN ('admin', 'user'));
DELETE FROM se_users WHERE username IN ('admin', 'user');

-- 插入测试管理员用户
INSERT INTO se_users(code, username, password, name, phone, email, bio, created_by, updated_by, created_time, updated_time)
VALUES ('11111111-1111-1111-1111-111111111111', 'admin',
        '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
        '测试管理员', '13800000001', 'admin@test.com', '测试管理员账号',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入测试普通用户
INSERT INTO se_users(code, username, password, name, phone, email, bio, created_by, updated_by, created_time, updated_time)
VALUES ('22222222-2222-2222-2222-222222222222', 'user',
        '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
        '测试普通用户', '13800000002', 'user@test.com', '测试普通用户账号',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 为测试管理员分配权限
INSERT INTO se_authorities(code, user_code, authority, created_by, updated_by, created_time, updated_time)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'ROLE_SYSTEM_ADMINISTRATORS',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO se_authorities(code, user_code, authority, created_by, updated_by, created_time, updated_time)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111', 'ROLE_ADMINISTRATORS',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 为测试普通用户分配权限
INSERT INTO se_authorities(code, user_code, authority, created_by, updated_by, created_time, updated_time)
VALUES ('cccccccc-cccc-cccc-cccc-cccccccccccc', '22222222-2222-2222-2222-222222222222', 'ROLE_USER',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 创建测试用户组
INSERT INTO se_groups(code, name, description, created_by, updated_by, created_time, updated_time)
VALUES ('33333333-3333-3333-3333-333333333333', '测试管理员组', '用于集成测试的管理员组',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO se_groups(code, name, description, created_by, updated_by, created_time, updated_time)
VALUES ('44444444-4444-4444-4444-444444444444', '测试用户组', '用于集成测试的普通用户组',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 将测试用户加入对应组
INSERT INTO se_group_members(code, group_code, user_code, created_by, updated_by, created_time, updated_time)
VALUES ('55555555-5555-5555-5555-555555555555', '33333333-3333-3333-3333-333333333333',
        '11111111-1111-1111-1111-111111111111',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO se_group_members(code, group_code, user_code, created_by, updated_by, created_time, updated_time)
VALUES ('66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444',
        '22222222-2222-2222-2222-222222222222',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 为测试组分配权限
INSERT INTO se_group_authorities(code, group_code, authority, created_by, updated_by, created_time, updated_time)
VALUES ('77777777-7777-7777-7777-777777777777', '33333333-3333-3333-3333-333333333333', 'ROLE_ADMIN_GROUP',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO se_group_authorities(code, group_code, authority, created_by, updated_by, created_time, updated_time)
VALUES ('88888888-8888-8888-8888-888888888888', '44444444-4444-4444-4444-444444444444', 'ROLE_USER_GROUP',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入测试租户数据（如果需要）
INSERT INTO se_tenants(code, name, description, created_by, updated_by, created_time, updated_time)
VALUES ('99999999-9999-9999-9999-999999999999', '测试租户', '用于集成测试的租户',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 将测试用户加入测试租户
INSERT INTO se_tenant_members(code, tenant_code, user_code, created_by, updated_by, created_time, updated_time)
VALUES ('aaaabbbb-cccc-dddd-eeee-ffffgggghhh1', '99999999-9999-9999-9999-999999999999',
        '11111111-1111-1111-1111-111111111111',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO se_tenant_members(code, tenant_code, user_code, created_by, updated_by, created_time, updated_time)
VALUES ('aaaabbbb-cccc-dddd-eeee-ffffgggghhh2', '99999999-9999-9999-9999-999999999999',
        '22222222-2222-2222-2222-222222222222',
        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 提交事务
COMMIT;