do
$do$
    declare
        v_code      uuid;
        v_phone     varchar(64);
        batch_size  INTEGER := 100;
        batch_count INTEGER := 0;
    begin
        for i in 1..100
            loop
                -- 生成用户代码
                v_code := gen_random_uuid();
                -- 生成手机号码
                v_phone := '1708911826' || lpad(i::text, 2, '0');

                -- 插入用户数据
                insert into se_users(code, username, password, name, phone, email, bio, created_by, updated_by)
                values (v_code, 'user' || i,
                        '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
                        '普通用户' || i, v_phone, 'user' || i || '@qq.com', null,
                        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479');

                insert into se_authorities(code, user_code, authority, created_by, updated_by)
                values (gen_random_uuid(), v_code, 'ROLE_USER',
                        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479');
                batch_count := batch_count + 1;
                if (batch_count % batch_size = 0) then
                    commit;
                end if;
            end loop;
        commit;
    end;
$do$;